package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EndlessObedience;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndergrowthScavenger.class, RuneclawBear.class, LightningStrike.class, EndlessObedience.class})
class UndergrowthScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in any graveyard")
    void entersWithCountersPerCreatureCardInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new RuneclawBear());
        gd.playerGraveyards.get(player2.getId()).add(new RuneclawBear());
        gd.playerGraveyards.get(player2.getId()).add(new RuneclawBear());
        gd.playerGraveyards.get(player1.getId()).add(new LightningStrike()); // not a creature card

        castScavenger();

        Permanent scavenger = findPermanent(player1, "Undergrowth Scavenger");
        assertThat(scavenger).isNotNull();
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(scavenger.getEffectivePower()).isEqualTo(3);
        assertThat(scavenger.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With no creature cards in any graveyard it enters as a 0/0 and dies")
    void diesWithNoCreatureCardsInGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new LightningStrike());

        castScavenger();

        harness.assertNotOnBattlefield(player1, "Undergrowth Scavenger");
    }

    @Test
    @DisplayName("Counts creatures that die while the creature spell is on the stack")
    void countsGraveyardsAtEntryRatherThanAtCasting() {
        harness.addToBattlefield(player1, new RuneclawBear());
        Permanent bear = findPermanent(player1, "Runeclaw Bear");
        harness.setHand(player1, List.of(new UndergrowthScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.passBothPriorities();

        Permanent scavenger = findPermanent(player1, "Undergrowth Scavenger");
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts creature cards even when they are all in the opponent's graveyard")
    void countsOnlyOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new RuneclawBear(), new RuneclawBear()));

        castScavenger();

        Permanent scavenger = findPermanent(player1, "Undergrowth Scavenger");
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters do not change when creature cards later leave graveyards")
    void countersAreFixedAfterEntry() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        castScavenger();

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        Permanent scavenger = findPermanent(player1, "Undergrowth Scavenger");
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scavenger.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts itself when returned from a graveyard")
    void countsItselfWhenReanimatedFromOpponentsGraveyard() {
        UndergrowthScavenger card = new UndergrowthScavenger();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new EndlessObedience()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, card.getId());

        Permanent scavenger = findPermanent(player1, "Undergrowth Scavenger");
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Undergrowth Scavenger");
        assertThat(gd.stack).isEmpty();
    }
    private void castScavenger() {
        harness.setHand(player1, List.of(new UndergrowthScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // 3 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell
    }
}
