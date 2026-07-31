package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Smelt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts you control have indestructible, including Darksteel Forge itself")
    void grantsIndestructibleToOwnArtifactsIncludingSelf() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player1, new FountainOfYouth());

        Permanent forge = findPermanent(player1, "Darksteel Forge");
        Permanent fountain = findPermanent(player1, "Fountain of Youth");
        assertThat(gqs.hasKeyword(gd, forge, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, fountain, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Nonartifact creatures you control do not get indestructible")
    void doesNotAffectNonartifactCreatures() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not affect artifacts an opponent controls")
    void doesNotAffectOpponentArtifacts() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player2, new FountainOfYouth());

        Permanent fountain = findPermanent(player2, "Fountain of Youth");
        assertThat(gqs.hasKeyword(gd, fountain, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Own artifacts survive Smelt")
    void protectedArtifactsSurviveSmelt() {
        harness.addToBattlefield(player1, new DarksteelForge());
        harness.addToBattlefield(player1, new FountainOfYouth());

        harness.setHand(player2, List.of(new Smelt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Darksteel Forge");
        harness.assertInGraveyard(player2, "Smelt");
    }
}
