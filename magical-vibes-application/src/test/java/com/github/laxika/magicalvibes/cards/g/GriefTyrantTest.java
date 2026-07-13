package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GriefTyrantTest extends BaseCardTest {

    // ===== Enters with four -1/-1 counters =====

    @Test
    @DisplayName("Enters the battlefield with four -1/-1 counters (an 8/8 becomes a 4/4)")
    void entersWithFourMinusCounters() {
        harness.setHand(player1, List.of(new GriefTyrant()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent tyrant = findPermanent(player1, "Grief Tyrant");
        assertThat(tyrant).isNotNull();
        assertThat(tyrant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, tyrant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tyrant)).isEqualTo(4);
    }

    // ===== Death trigger: hands out -1/-1 counters equal to its own =====

    @Test
    @DisplayName("On death, puts a -1/-1 counter on target creature for each -1/-1 counter it had")
    void deathTriggerPutsCountersOnTarget() {
        Permanent tyrant = addCreatureReady(player1, new GriefTyrant());
        tyrant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        tyrant.tap(); // so it can be Assassinated
        harness.addToBattlefield(player2, new HillGiant());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, tyrant.getId(), null);
        harness.passBothPriorities(); // Assassinate resolves — Grief Tyrant dies, death trigger asks for a target

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities(); // resolve the death trigger

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1); // 3/3 -> 1/1
    }

    @Test
    @DisplayName("Death trigger does nothing when there is no creature to target")
    void deathTriggerNeedsCreatureTarget() {
        Permanent tyrant = addCreatureReady(player1, new GriefTyrant());
        tyrant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        tyrant.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, tyrant.getId(), null);
        harness.passBothPriorities(); // Grief Tyrant dies with no other creature on the battlefield

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }
}
