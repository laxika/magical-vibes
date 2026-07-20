package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulstingerTest extends BaseCardTest {

    // ===== ETB: two -1/-1 counters on target creature you control =====

    @Test
    @DisplayName("ETB puts two -1/-1 counters on a creature you control")
    void etbPutsTwoCountersOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new Soulstinger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, elemental.getId(), null);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Air Elemental (4/4) with two -1/-1 counters → 2/2.
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(elemental.getEffectivePower()).isEqualTo(2);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB cannot target a creature you don't control")
    void etbCannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Soulstinger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, opponentCreature, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    // ===== Death trigger: hand out -1/-1 counters equal to its own (you may, any creature) =====

    @Test
    @DisplayName("On death, may put a -1/-1 counter on target creature — including an opponent's — for each on it")
    void deathTriggerTargetsAnyCreatureIncludingOpponents() {
        Permanent stinger = addCreatureReady(player1, new Soulstinger());
        stinger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        stinger.tap(); // so it can be Assassinated
        harness.addToBattlefield(player2, new HillGiant());

        // player1 controls Soulstinger, so the death trigger is player1's — resolve it during player1's
        // own turn by having player1 Assassinate their own tapped Soulstinger.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, stinger.getId(), null);
        harness.passBothPriorities(); // Assassinate resolves — Soulstinger dies, death trigger asks for a target

        // The death trigger's target is ANY creature (not restricted to the ETB's "you control"),
        // so the opponent's Hill Giant is a legal target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities(); // resolve the death trigger → "you may" prompt
        harness.handleMayAbilityChosen(player1, true);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1); // 3/3 → 1/1
    }

    @Test
    @DisplayName("Death trigger is optional — declining places no counters")
    void deathTriggerCanBeDeclined() {
        Permanent stinger = addCreatureReady(player1, new Soulstinger());
        stinger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        stinger.tap();
        harness.addToBattlefield(player2, new HillGiant());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, stinger.getId(), null);
        harness.passBothPriorities();

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }
}
