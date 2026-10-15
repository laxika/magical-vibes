package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcademyElite.class, Counterspell.class, DeepAnalysis.class, WildMongrel.class})
class AcademyEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters for instant and sorcery cards in all graveyards")
    void entersWithCountersForAllGraveyards() {
        harness.setGraveyard(player1, List.of(new Counterspell()));
        harness.setGraveyard(player2, List.of(new DeepAnalysis(), new WildMongrel()));
        harness.setHand(player1, List.of(new AcademyElite()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent elite = findPermanent(player1, "Academy Elite");
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a counter draws a card, then discards a card")
    void removesCounterToDrawThenDiscard() {
        Permanent elite = addCreatureReady(player1, new AcademyElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card discarded = new WildMongrel();
        Card drawn = new Counterspell();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("The ability cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent elite = addCreatureReady(player1, new AcademyElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Dies on entry when no graveyard contains an instant or sorcery")
    void diesOnEntryWithoutMatchingCards() {
        harness.setGraveyard(player1, List.of(new WildMongrel()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new AcademyElite()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Academy Elite");
        harness.assertInGraveyard(player1, "Academy Elite");
    }

    @Test
    @DisplayName("Removing the last counter kills the creature but its ability still resolves")
    void lastCounterAbilityResolvesAfterSourceDies() {
        Permanent elite = addCreatureReady(player1, new AcademyElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card kept = new WildMongrel();
        Card drawn = new Counterspell();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Academy Elite");
        harness.assertInGraveyard(player1, "Academy Elite");
        assertThat(gd.playerHands.get(player1.getId())).contains(kept, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(drawn));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Counter count is fixed on entry and does not track subsequent graveyard changes")
    void counterCountDoesNotTrackGraveyardChanges() {
        harness.setGraveyard(player1, List.of(new Counterspell()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new AcademyElite()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent elite = findPermanent(player1, "Academy Elite");
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Counterspell(), new DeepAnalysis()));

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
