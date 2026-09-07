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
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not deal damage when a spell's mana spent equals its mana value")
    void doesNotDamageWhenManaSpentEqualsManaValue() {
        harness.addToBattlefield(player1, new TokkaRahzarTerribleTwos());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage to an opponent who casts a spell for less than its mana value")
    void damagesOpponentCastingReducedSpell() {
        harness.addToBattlefield(player1, new TokkaRahzarTerribleTwos());
        harness.addToBattlefield(player2, new GoblinElectromancer());
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
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
