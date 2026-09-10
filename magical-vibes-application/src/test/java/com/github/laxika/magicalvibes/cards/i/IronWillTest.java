package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronWill.class, GiantCockroach.class, Crawlspace.class})
class IronWillTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Iron Will gives target creature +0/+4")
    void boostsTargetCreature() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new IronWill()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        assertThat(cockroach.getEffectivePower()).isEqualTo(4);
        assertThat(cockroach.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Iron Will can target a creature an opponent controls")
    void boostsOpponentsCreature() {
        Permanent opponentCockroach = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new IronWill()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, opponentCockroach.getId());

        assertThat(opponentCockroach.getEffectivePower()).isEqualTo(4);
        assertThat(opponentCockroach.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Iron Will's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new IronWill()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cockroach.getEffectivePower()).isEqualTo(4);
        assertThat(cockroach.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Iron Will cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent crawlspace = harness.addToBattlefieldAndReturn(player1, new Crawlspace());
        harness.setHand(player1, List.of(new IronWill()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, crawlspace.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling Iron Will discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new IronWill()));
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Iron Will");
        harness.assertInHand(player1, "Giant Cockroach");
    }
}
