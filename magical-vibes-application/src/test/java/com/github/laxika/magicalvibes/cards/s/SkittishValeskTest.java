package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkittishValesk.class)
class SkittishValeskTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SkittishValesk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent valesk = findPermanent(player1, "Skittish Valesk");
        assertThat(valesk.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, valesk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valesk)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(valesk));
        harness.passBothPriorities();

        assertThat(valesk.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("At the beginning of its controller's upkeep, it turns face down on a lost flip")
    void turnsFaceDownOnLostUpkeepFlip() {
        Permanent valesk = harness.addToBattlefieldAndReturn(player1, new SkittishValesk());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        boolean wonFlip = gameLogContains("wins the coin flip for Skittish Valesk");
        boolean lostFlip = gameLogContains("loses the coin flip for Skittish Valesk");
        assertThat(wonFlip).isNotEqualTo(lostFlip);
        assertThat(valesk.isFaceDown()).isEqualTo(lostFlip);
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent valesk = harness.addToBattlefieldAndReturn(player1, new SkittishValesk());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gameLogContains("coin flip for Skittish Valesk")).isFalse();
        assertThat(valesk.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("A face-down Valesk has no upkeep ability")
    void faceDownValeskDoesNotTriggerAtUpkeep() {
        harness.setHand(player1, List.of(new SkittishValesk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent valesk = findPermanent(player1, "Skittish Valesk");
        assertThat(valesk.isFaceDown()).isTrue();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gameLogContains("coin flip for Skittish Valesk")).isFalse();
        assertThat(valesk.isFaceDown()).isTrue();
    }
}
