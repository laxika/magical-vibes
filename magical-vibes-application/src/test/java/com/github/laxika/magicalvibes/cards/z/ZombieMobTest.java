package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.s.SabertoothCobra;
import com.github.laxika.magicalvibes.cards.s.ShallowGrave;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZombieMob.class, SabertoothCobra.class, DarkBanishing.class, ShallowGrave.class})
class ZombieMobTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in controller's graveyard")
    void entersWithCountersPerCreatureCard() {
        harness.setGraveyard(player1, List.of(
                new SabertoothCobra(), new SabertoothCobra(), new DarkBanishing()));

        castMob();

        Permanent mob = findPermanent(player1, "Zombie Mob");
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles all creature cards from controller's graveyard, leaving non-creature cards")
    void exilesCreatureCardsFromGraveyard() {
        harness.setGraveyard(player1, List.of(
                new SabertoothCobra(), new SabertoothCobra(), new DarkBanishing()));

        castMob();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Dark Banishing");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Sabertooth Cobra", "Sabertooth Cobra");
    }

    @Test
    @DisplayName("Does not touch the opponent's graveyard")
    void leavesOpponentGraveyardAlone() {
        harness.setGraveyard(player1, List.of(new SabertoothCobra()));
        harness.setGraveyard(player2, List.of(new SabertoothCobra()));

        castMob();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Zombie Mob")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts itself when it enters from its controller's graveyard")
    void countsItselfWhenReturnedFromGraveyard() {
        harness.setGraveyard(player1, List.of(new SabertoothCobra(), new ZombieMob()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        resolveAllTriggers();

        Permanent mob = findPermanent(player1, "Zombie Mob");
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Shallow Grave");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Sabertooth Cobra");
    }

    @Test
    @DisplayName("With an empty graveyard it enters as a 2/0 and dies to state-based actions")
    void diesWithNoCreatureCardsInGraveyard() {
        castMob();

        harness.assertNotOnBattlefield(player1, "Zombie Mob");
    }

    private void castMob() {
        harness.castFromHand(player1, new ZombieMob(), "{2}{B}{B}");
        resolveAllTriggers();
    }
}
