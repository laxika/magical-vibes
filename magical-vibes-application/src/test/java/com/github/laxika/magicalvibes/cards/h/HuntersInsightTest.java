package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuntersInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the combat damage the chosen creature deals to a player")
    void drawsEqualToCombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        bears.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        // Grizzly Bears dealt 2 combat damage, so two cards are drawn.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }

    @Test
    @DisplayName("The granted trigger wears off at end of turn")
    void triggerWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        bears.setAttacking(true);
        resolveCombat();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
