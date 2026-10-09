package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelForge.class, DrossGolem.class, CrazedGoblin.class,
        BarbedLightning.class, Oxidize.class, EchoingTruth.class})
class DarksteelForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts you control have indestructible, including Darksteel Forge itself")
    void grantsIndestructibleToOwnArtifactsIncludingSelf() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new DarksteelForge());
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new DrossGolem());
        assertThat(gqs.hasKeyword(gd, forge, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Nonartifact creatures you control do not get indestructible")
    void doesNotAffectNonartifactCreatures() {
        harness.addToBattlefield(player1, new DarksteelForge());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect artifacts an opponent controls")
    void doesNotAffectOpponentArtifacts() {
        harness.addToBattlefield(player1, new DarksteelForge());
        Permanent golem = harness.addToBattlefieldAndReturn(player2, new DrossGolem());
        assertThat(gqs.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Own artifacts survive Oxidize")
    void protectedArtifactsSurviveOxidize() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player1, new DrossGolem());

        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID targetId = harness.getPermanentId(player1, "Dross Golem");
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertOnBattlefield(player1, "Dross Golem");
        harness.assertOnBattlefield(player1, "Darksteel Forge");
        harness.assertInGraveyard(player2, "Oxidize");
    }

    @Test
    @DisplayName("An artifact creature survives lethal damage")
    void protectedArtifactCreatureSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new DarksteelForge());
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new DrossGolem());

        harness.setHand(player2, List.of(new BarbedLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(golem.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dross Golem");
        assertThat(golem.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Darksteel Forge itself survives a destroy effect")
    void forgeSurvivesOxidize() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new DarksteelForge());
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, forge.getId());

        harness.assertOnBattlefield(player1, "Darksteel Forge");
        harness.assertNotInGraveyard(player1, "Darksteel Forge");
        harness.assertInGraveyard(player2, "Oxidize");
    }

    @Test
    @DisplayName("An opponent's artifact is still destroyed")
    void opponentArtifactIsDestroyed() {
        harness.addToBattlefield(player1, new DarksteelForge());
        Permanent golem = harness.addToBattlefieldAndReturn(player2, new DrossGolem());
        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, golem.getId());

        harness.assertNotOnBattlefield(player2, "Dross Golem");
        harness.assertInGraveyard(player2, "Dross Golem");
    }

    @Test
    @DisplayName("An existing artifact gains protection when Darksteel Forge enters")
    void existingArtifactGainsIndestructible() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new DrossGolem());
        assertThat(gqs.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.setHand(player1, List.of(new DarksteelForge()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertOnBattlefield(player1, "Darksteel Forge");
    }

    @Test
    @DisplayName("Lethally damaged artifacts die when Darksteel Forge leaves the battlefield")
    void lethalDamageKillsArtifactAfterForgeLeaves() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new DarksteelForge());
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new DrossGolem());
        harness.setHand(player2, List.of(new BarbedLightning(), new EchoingTruth()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(golem.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dross Golem");
        assertThat(golem.getMarkedDamage()).isEqualTo(3);

        harness.castAndResolveInstant(player2, 0, forge.getId());

        harness.assertInHand(player1, "Darksteel Forge");
        harness.assertNotOnBattlefield(player1, "Darksteel Forge");
        harness.assertNotOnBattlefield(player1, "Dross Golem");
        harness.assertInGraveyard(player1, "Dross Golem");
    }
}
