package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentinelSarahLyons.class, ChromaticStar.class, GrizzlyBears.class})
class SentinelSarahLyonsTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts entering under your control give your creatures +2/+2")
    void boostsOwnCreaturesAfterArtifactEntry() {
        Permanent sarah = addCreatureReady(player1, new SentinelSarahLyons());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, sarah)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());

        assertThat(gqs.getEffectivePower(gd, sarah)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sarah)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Battalion deals damage equal to your artifact count")
    void battalionDealsArtifactCountDamage() {
        addCreatureReady(player1, new SentinelSarahLyons());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addToBattlefield(player1, new ChromaticStar());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 10);
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        addCreatureReady(player1, new SentinelSarahLyons());
        addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    void opponentArtifactEntryDoesNotEnableBoost() {
        Permanent sarah = addCreatureReady(player1, new SentinelSarahLyons());
        harness.enterBattlefieldAndReturn(player2, new ChromaticStar());

        assertThat(gqs.getEffectivePower(gd, sarah)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sarah)).isEqualTo(4);
    }

    @Test
    void earlierArtifactEntryBoostsSarahAndLaterCreaturesButNotOpponents() {
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        Permanent sarah = harness.enterBattlefieldAndReturn(player1, new SentinelSarahLyons());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());

        assertThat(gqs.getEffectivePower(gd, sarah)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sarah)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    void boostPersistsAfterArtifactLeaves() {
        Permanent sarah = addCreatureReady(player1, new SentinelSarahLyons());
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.withAutoStop(gd.currentStep, () -> {
            harness.handleListChoice(player1, "WHITE");
            harness.passBothPriorities();
        });

        harness.assertNotOnBattlefield(player1, "Chromatic Star");
        assertThat(gqs.getEffectivePower(gd, sarah)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sarah)).isEqualTo(6);
    }

    @Test
    void boostExpiresOnNextTurnEvenIfArtifactRemains() {
        Permanent sarah = addCreatureReady(player1, new SentinelSarahLyons());
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Chromatic Star");
        assertThat(gqs.getEffectivePower(gd, sarah)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sarah)).isEqualTo(4);
    }

    @Test
    void battalionCanTargetControllerAndCountsArtifactsAtResolution() {
        addCreatureReady(player1, new SentinelSarahLyons());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addToBattlefield(player2, new ChromaticStar());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0, 1, 2));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, player1.getId()));
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void battalionRequiresSarahToAttack() {
        addCreatureReady(player1, new SentinelSarahLyons());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ChromaticStar());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(1, 2, 3));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    void hasteAllowsSarahToAttackOnEntryTurn() {
        harness.enterBattlefieldAndReturn(player1, new SentinelSarahLyons());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void battalionTriggersWithNoArtifactsAndDealsNoDamage() {
        addCreatureReady(player1, new SentinelSarahLyons());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0, 1, 2));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
