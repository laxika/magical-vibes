package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.h.Hydroblast;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.j.JaceThePerfectedMind;
import com.github.laxika.magicalvibes.cards.r.RevivingVapors;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultBattery.class, Desert.class, Dodecapod.class, InvasionOfInnistrad.class,
        JaceThePerfectedMind.class, Hydroblast.class, RevivingVapors.class})
class AssaultBatteryTest extends BaseCardTest {

    @Test
    @DisplayName("Assault deals 2 damage to a creature")
    void assaultDealsDamageToCreature() {
        harness.addToBattlefield(player2, new Dodecapod());
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player2, "Dodecapod"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Dodecapod").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Assault deals 2 damage to a planeswalker")
    void assaultDealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceThePerfectedMind());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Assault deals 2 damage to a player")
    void assaultDealsDamageToPlayer() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Assault can target a battle")
    void assaultDealsDamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Battery creates a 3/3 green Elephant token for its own cost")
    void batteryCreatesElephantToken() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        List<Permanent> elephants = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elephant"))
                .toList();
        assertThat(elephants).hasSize(1);
        assertThat(elephants.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getColors()).contains(CardColor.GREEN);
        assertThat(elephants.getFirst().getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
    }

    @Test
    @DisplayName("Assault cannot target a land")
    void assaultCannotTargetLand() {
        harness.addToBattlefield(player2, new Desert());
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 0, harness.getPermanentId(player2, "Desert")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Assault can deal damage to its controller")
    void assaultCanTargetItsController() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Elephant");
    }

    @Test
    @DisplayName("Battery cannot be cast for Assault's red mana cost")
    void batteryCannotUseAssaultCost() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Elephant");
    }

    @Test
    @DisplayName("Assault requires red mana even when Battery's cost can be paid")
    void assaultCannotUseBatteryCost() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Hydroblast cannot counter the green Battery half")
    void batteryIsNotRedForHydroblast() {
        AssaultBattery spell = new AssaultBattery();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Hydroblast()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elephant");
        harness.assertInGraveyard(player2, "Hydroblast");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Reviving Vapors gains life for both halves' combined mana value")
    void revivingVaporsUsesCombinedManaValue() {
        AssaultBattery chosen = new AssaultBattery();
        AssaultBattery restOne = new AssaultBattery();
        AssaultBattery restTwo = new AssaultBattery();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        harness.castFromHand(player1, new RevivingVapors(), "{2}{W}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.assertLife(player1, 25);
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
    }
}
