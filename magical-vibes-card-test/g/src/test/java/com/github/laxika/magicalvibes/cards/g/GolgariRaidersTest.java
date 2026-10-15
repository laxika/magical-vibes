package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgariRaiders.class, PreyUpon.class})
class GolgariRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in its controller's graveyard")
    void entersWithCountersPerCreatureCard() {
        harness.setGraveyard(player1, List.of(new GolgariRaiders(), new GolgariRaiders(), new PreyUpon()));
        harness.setGraveyard(player2, List.of(new GolgariRaiders()));

        castRaiders();

        Permanent raiders = findPermanent(player1, "Golgari Raiders");
        assertThat(raiders).isNotNull();
        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(raiders.getEffectivePower()).isEqualTo(2);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("With no creature cards in its controller's graveyard it enters as a 0/0 and dies")
    void diesWithNoCreatureCardsInGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new PreyUpon());

        castRaiders();

        harness.assertNotOnBattlefield(player1, "Golgari Raiders");
    }

    @Test
    @DisplayName("Haste allows it to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        gd.playerGraveyards.get(player1.getId()).add(new GolgariRaiders());
        harness.setLife(player2, 20);

        castRaiders();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void castRaiders() {
        harness.setHand(player1, List.of(new GolgariRaiders()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counts creature cards at entry rather than when the spell is cast")
    void countsGraveyardAtEntry() {
        harness.setHand(player1, List.of(new GolgariRaiders()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        harness.setGraveyard(player1, List.of(new GolgariRaiders(), new GolgariRaiders()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Golgari Raiders")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters remain unchanged when creature cards leave the graveyard after entry")
    void countersDoNotTrackLaterGraveyardChanges() {
        harness.setGraveyard(player1, List.of(new GolgariRaiders(), new GolgariRaiders()));
        castRaiders();

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        Permanent raiders = findPermanent(player1, "Golgari Raiders");
        assertThat(raiders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(raiders.getEffectivePower()).isEqualTo(2);
        assertThat(raiders.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creature cards cannot keep Raiders alive with an empty controller graveyard")
    void opponentsGraveyardDoesNotSupplyCounters() {
        harness.setGraveyard(player2, List.of(new GolgariRaiders(), new GolgariRaiders()));

        castRaiders();

        harness.assertNotOnBattlefield(player1, "Golgari Raiders");
        harness.assertInGraveyard(player1, "Golgari Raiders");
    }

}
