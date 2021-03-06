package com.sap.piper

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import util.BasePiperTest
import util.JenkinsLoggingRule
import util.JenkinsReadFileRule
import util.JenkinsWriteFileRule
import util.Rules

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.allOf
import static org.junit.Assert.assertThat

class WhitesourceConfigurationHelperTest extends BasePiperTest {
    JenkinsReadFileRule jrfr = new JenkinsReadFileRule(this, 'test/resources/utilsTest/')
    JenkinsWriteFileRule jwfr = new JenkinsWriteFileRule(this)
    JenkinsLoggingRule jlr = new JenkinsLoggingRule(this)

    @Rule
    public RuleChain ruleChain = Rules
        .getCommonRules(this)
        .around(jrfr)
        .around(jwfr)
        .around(jlr)

    @Before
    void init() {
        def p = new Properties()
        p.put("log.level", "debug")
        helper.registerAllowedMethod('readProperties', [Map], {return p})
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentEmptyConfig() {
        helper.registerAllowedMethod('readProperties', [Map], {return new Properties()})
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'none', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
                    allOf(
                        not(containsString("log.level=debug")),
                        containsString("apiKey=abcd"),
                        containsString("productName=DIST - name1"),
                        containsString("productToken=1234"),
                        containsString("userKey=0000")
                    )
        )

        assertThat(jlr.log, containsString("[Whitesource] Configuration for scanType: 'none' is not yet hardened, please do a quality assessment of your scan results."))
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentConfigDeeper() {
        helper.registerAllowedMethod('readProperties', [Map], { m -> if (!m.file.contains('testModule')) return new Properties() else return null })
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'none', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./testModule/")
        assertThat(jwfr.files['./testModule/config.13954509c7675edfce373138f51c68464d1abcac'],
            allOf(
                not(containsString("log.level=debug")),
                containsString("apiKey=abcd"),
                containsString("productName=DIST - name1"),
                containsString("productToken=1234"),
                containsString("userKey=0000")
            )
        )

        assertThat(jlr.log, containsString("[Whitesource] Configuration for scanType: 'none' is not yet hardened, please do a quality assessment of your scan results."))
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentMaven() {
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'none', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=abcd"),
                containsString("productName=DIST - name1"),
                containsString("productToken=1234"),
                containsString("userKey=0000")
            )
        )

        assertThat(jlr.log, containsString("[Whitesource] Configuration for scanType: 'none' is not yet hardened, please do a quality assessment of your scan results."))
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentNpm() {
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'npm', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=abcd"),
                containsString("productName=DIST - name1"),
                containsString("productToken=1234"),
                containsString("userKey=0000")
            )
        )

        assertThat(jlr.log, containsString("[Whitesource] Configuration for scanType: 'npm' is not yet hardened, please do a quality assessment of your scan results."))
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentDocker() {
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'docker', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=abcd"),
                containsString("productName=DIST - name1"),
                containsString("productToken=1234"),
                containsString("docker.scanImages=true"),
                containsString("docker.scanTarFiles=true"),
                containsString("docker.includes=.*.tar"),
            )
        )
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentSbt() {
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'sbt', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=abcd"),
                containsString("productName=DIST - name1"),
                containsString("productToken=1234"),
                containsString("userKey=0000"),
                containsString("sbt.resolveDependencies=true"),
                containsString("log.level=debug")
            )
        )
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentDub() {
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'dub', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=abcd"),
                containsString("productName=DIST - name1"),
                containsString("productToken=1234"),
                containsString("userKey=0000"),
                containsString("includes=**/*.d **/*.di")
            )
        )
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentPip() {
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'pip', whitesource: [configFilePath: './config',serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'DIST - name1', productToken: '1234', userKey: '0000']], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=abcd"),
                containsString("productName=DIST - name1"),
                containsString("productToken=1234"),
                containsString("userKey=0000"),
                containsString("python.resolveDependencies=true")
            )
        )

        assertThat(jlr.log, not(containsString("[Whitesource] Configuration for scanType: 'pip' is not yet hardened, please do a quality assessment of your scan results.")))
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentGolangVerbose() {
        def config = [scanType: 'golang', whitesource: [configFilePath: './config', serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'abcd', productName: 'SHC - name2', productToken: '1234', userKey: '0000'], stashContent: ['some', 'stashes'], verbose: true]
        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, config, "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=abcd"),
                containsString("productName=SHC - name2"),
                containsString("productToken=1234"),
                containsString("userKey=0000"),
                containsString("go.resolveDependencies=true"),
                containsString("log.level=debug")
            )
        )

        assertThat(config.stashContent, hasItem(containsString('modified whitesource config ')))
        assertThat(jlr.log, not(containsString("[Warning][Whitesource] Configuration for scanType: 'golang' is not yet hardened, please do a quality assessment of your scan results.")))
    }

    @Test
    void testExtendConfigurationFileUnifiedAgentEnforcement() {
        def p = new Properties()
        p.putAll(['python.resolveDependencies': 'false', 'python.ignoreSourceFiles': 'false', 'python.ignorePipInstallErrors': 'true','python.installVirtualenv': 'false'])
        helper.registerAllowedMethod('readProperties', [Map], {return p})

        WhitesourceConfigurationHelper.extendUAConfigurationFile(nullScript, utils, [scanType: 'pip', whitesource: [configFilePath: './config', serviceUrl: "http://some.host.whitesource.com/api/", orgToken: 'cdfg', productName: 'name', productToken: '1234', userKey: '0000'], verbose: true], "./")
        assertThat(jwfr.files['./config.847f9aec2f93de9000d5fa4e6eaace2283ae6377'],
            allOf(
                containsString("apiKey=cdfg"),
                containsString("productName=name"),
                containsString("productToken=1234"),
                containsString("userKey=0000"),
                containsString("python.resolveDependencies=true"),
                containsString("log.level=debug"),
                containsString("python.resolveDependencies=true"),
                containsString("python.ignoreSourceFiles=true"),
                containsString("python.ignorePipInstallErrors=true"),
                containsString("python.installVirtualenv=false")
            )
        )
    }
}

