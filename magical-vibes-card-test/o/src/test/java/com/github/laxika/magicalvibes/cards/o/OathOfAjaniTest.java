package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AjaniUnyielding;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OathOfAjani.class, GrizzlyBears.class, AjaniUnyielding.class, DruidOfTheCowl.class})
class OathOfAjaniTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, puts a +1/+1 counter on each creature you control")
    void entersWithCountersOnControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OathOfAjani()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Planeswalker spells you cast cost one less to cast")
    void reducesPlaneswalkerSpellCost() {
        harness.addToBattlefield(player1, new OathOfAjani());
        harness.setHand(player1, List.of(testPlaneswalker("Test Walker", "{1}{G}")));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-planeswalker spells are not reduced")
    void doesNotReduceNonPlaneswalkerSpellCost() {
        harness.addToBattlefield(player1, new OathOfAjani());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesRealPlaneswalkerGenericCost() {
        harness.addToBattlefield(player1, new OathOfAjani());
        harness.setHand(player1, List.of(new AjaniUnyielding()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ajani Unyielding");
    }

    @Test
    void doesNotReduceOpponentsPlaneswalkerCost() {
        harness.addToBattlefield(player2, new OathOfAjani());
        harness.setHand(player1, List.of(new AjaniUnyielding()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceColoredManaRequirements() {
        harness.addToBattlefield(player1, new OathOfAjani());
        harness.setHand(player1, List.of(new AjaniUnyielding()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersApplyToEveryCreaturePresentWhenTriggerResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new AjaniUnyielding());
        harness.setHand(player1, List.of(new OathOfAjani()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        assertThat(laterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithNoCreaturesAndStillReducesPlaneswalkerCost() {
        harness.setHand(player1, List.of(new OathOfAjani(), new AjaniUnyielding()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castPlaneswalker(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }
    private Card testPlaneswalker(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
        card.setManaCost(manaCost);
        card.setLoyalty(3);
        return card;
    }
}
