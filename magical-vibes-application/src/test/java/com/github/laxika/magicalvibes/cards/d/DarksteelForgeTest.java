package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
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
        BarbedLightning.class, Oxidize.class})
class DarksteelForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts you control have indestructible, including Darksteel Forge itself")
    void grantsIndestructibleToOwnArtifactsIncludingSelf() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player1, new DrossGolem());

        Permanent forge = findPermanent(player1, "Darksteel Forge");
        Permanent golem = findPermanent(player1, "Dross Golem");
        assertThat(gqs.hasKeyword(gd, forge, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Nonartifact creatures you control do not get indestructible")
    void doesNotAffectNonartifactCreatures() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player1, new CrazedGoblin());

        Permanent goblin = findPermanent(player1, "Crazed Goblin");
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect artifacts an opponent controls")
    void doesNotAffectOpponentArtifacts() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player2, new DrossGolem());

        Permanent golem = findPermanent(player2, "Dross Golem");
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
}
