package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorAsgardsAvenger.class, RagingGoblin.class, SerraAngel.class, Shock.class})
class ThorAsgardsAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Another source deals 1 additional damage to an opponent")
    void anotherSourceDealsAdditionalDamageToOpponent() {
        harness.addToBattlefield(player1, new ThorAsgardsAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Another source deals 1 additional damage to an opponent's permanent")
    void anotherSourceDealsAdditionalDamageToOpponentPermanent() {
        harness.addToBattlefield(player1, new ThorAsgardsAvenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Thor does not increase his own combat damage")
    void doesNotIncreaseOwnCombatDamage() {
        harness.addToBattlefield(player1, new ThorAsgardsAvenger());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Thor increases another creature's combat damage")
    void increasesAnotherCreatureCombatDamage() {
        harness.addToBattlefield(player1, new ThorAsgardsAvenger());
        addCreatureReady(player1, new RagingGoblin());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage to its controller is not increased")
    void doesNotIncreaseDamageToController() {
        harness.addToBattlefield(player1, new ThorAsgardsAvenger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An opponent's source is not increased")
    void doesNotIncreaseOpponentsSource() {
        harness.addToBattlefield(player1, new ThorAsgardsAvenger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
