package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GoblinElectromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TokkaRahzarTerribleTwos.class, Counterspell.class, Divination.class, GoblinElectromancer.class})
class TokkaRahzarTerribleTwosTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to the caster when a spell is cast for less than its mana value")
    void damagesCasterWhenSpellCostsLessThanManaValue() {
        harness.addToBattlefield(player1, new TokkaRahzarTerribleTwos());
        harness.addToBattlefield(player1, new GoblinElectromancer());
        harness.castFromHand(player1, new Divination(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not deal damage when a spell's mana spent equals its mana value")
    void doesNotDamageWhenManaSpentEqualsManaValue() {
        harness.addToBattlefield(player1, new TokkaRahzarTerribleTwos());
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage to an opponent who casts a spell for less than its mana value")
    void damagesOpponentCastingReducedSpell() {
        harness.addToBattlefield(player1, new TokkaRahzarTerribleTwos());
        harness.addToBattlefield(player2, new GoblinElectromancer());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Divination(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Still damages the caster after the discounted spell is countered")
    void damagesCasterAfterSpellIsCountered() {
        harness.addToBattlefield(player1, new TokkaRahzarTerribleTwos());
        harness.addToBattlefield(player1, new GoblinElectromancer());
        Divination divination = new Divination();
        harness.castFromHand(player1, divination, "{1}{U}");
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, divination.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Divination");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple cost reductions cause only one damage trigger per spell")
    void multipleReductionsCauseOneTrigger() {
        harness.addToBattlefield(player1, new TokkaRahzarTerribleTwos());
        harness.addToBattlefield(player1, new GoblinElectromancer());
        harness.addToBattlefield(player1, new GoblinElectromancer());

        harness.castFromHand(player1, new Divination(), "{U}");
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Divination");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        TokkaRahzarTerribleTwos tokka = new TokkaRahzarTerribleTwos();
        harness.setHand(player1, List.of(tokka));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, tokka.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tokka & Rahzar, Terrible Twos");
        harness.assertInGraveyard(player2, "Counterspell");
    }
}
