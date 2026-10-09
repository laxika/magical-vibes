package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrosstownCourier.class, Forest.class, DrudgeBeetle.class})
class CrosstownCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player mills that many cards")
    void millsCardsEqualToCombatDamage() {
        addAttackingCourier(player1);
        setLibrary(player2, 5);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills as many cards as the boosted combat damage dealt")
    void millScalesWithDamage() {
        Permanent courier = addAttackingCourier(player1);
        courier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // deals 4
        setLibrary(player2, 6);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("No mill when the Courier is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingCourier(player1);
        Permanent blocker = addCreatureReady(player2, new DrudgeBeetle());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        setLibrary(player2, 5);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    void millsOnlyAvailableCardsWhenLibraryIsShort() {
        addAttackingCourier(player1);
        setLibrary(player2, 1);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void zeroPowerDealsNoDamageAndDoesNotTrigger() {
        Permanent courier = addAttackingCourier(player1);
        courier.setPowerModifier(-2);
        setLibrary(player2, 5);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void millAmountRemainsDamageDealtWhenPowerChangesBeforeResolution() {
        Permanent courier = addAttackingCourier(player1);
        setLibrary(player2, 5);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        courier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void millsTheDamagedPlayerWhenPlayerTwoAttacks() {
        addAttackingCourier(player2);
        setLibrary(player1, 5);
        setLibrary(player2, 5);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    private Permanent addAttackingCourier(Player player) {
        Permanent courier = addCreatureReady(player, new CrosstownCourier());
        courier.setAttacking(true);
        return courier;
    }

    private void setLibrary(Player player, int size) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Forest());
        }
        harness.setLibrary(player, cards);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        // Resolve the mill trigger without advancing the turn (opponent would draw).
        resolveAllTriggers();
    }
}
