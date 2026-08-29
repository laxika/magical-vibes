package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatedFirepower.class, GrizzlyBears.class, Shock.class})
class FatedFirepowerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X fire counters and adds them to spell damage dealt to an opponent")
    void entersWithXCountersAndBoostsSpellDamageToOpponent() {
        Permanent fatedFirepower = castFatedFirepower(2);

        assertThat(fatedFirepower.getCounterCount(CounterType.FIRE)).isEqualTo(2);

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Adds fire-counter damage to a spell targeting an opponent's permanent")
    void boostsSpellDamageToOpponentsPermanent() {
        castFatedFirepower(1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Adds fire-counter damage to combat damage dealt to an opponent")
    void boostsCombatDamageToOpponent() {
        castFatedFirepower(2);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not add damage to your own player or to an opponent's source")
    void doesNotBoostOwnOrOpponentsSources() {
        castFatedFirepower(2);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    private Permanent castFatedFirepower(int xValue) {
        harness.setHand(player1, List.of(new FatedFirepower()));
        harness.addMana(player1, ManaColor.RED, xValue + 3);
        gs.playCard(gd, player1, 0, xValue, null, null);
        harness.passBothPriorities();
        return findPermanent(player1, "Fated Firepower");
    }
}
