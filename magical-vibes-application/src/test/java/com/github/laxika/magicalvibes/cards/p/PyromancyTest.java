package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FleetingImage;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.cards.m.Miscalculation;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({Pyromancy.class, Miscalculation.class, FleetingImage.class, PygmyPyrosaur.class,
        GrimMonolith.class, Ornithopter.class, ChandraNalaar.class})
class PyromancyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to a player equal to the random discarded card's mana value")
    void dealsDiscardedManaValueDamageToPlayer() {
        harness.addToBattlefield(player1, new Pyromancy());
        harness.setHand(player1, List.of(new Miscalculation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Miscalculation");
    }

    @Test
    @DisplayName("Deals the discarded mana value to a creature")
    void dealsDiscardedManaValueDamageToCreature() {
        harness.addToBattlefield(player1, new Pyromancy());
        Permanent target = addCreatureReady(player2, new PygmyPyrosaur());
        harness.setHand(player1, List.of(new FleetingImage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fleeting Image");
        harness.assertInGraveyard(player2, "Pygmy Pyrosaur");
    }

    @Test
    @DisplayName("Deals the discarded mana value to a planeswalker")
    void dealsDiscardedManaValueDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new Pyromancy());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new FleetingImage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("A mana-value-zero discarded card deals no damage")
    void zeroManaValueDealsNoDamage() {
        harness.addToBattlefield(player1, new Pyromancy());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new Pyromancy());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard at random");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Pyromancy());
        harness.setHand(player1, List.of(new Miscalculation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be");
    }

    @Test
    @DisplayName("Uses the activation's discarded card after another card is discarded in response")
    void usesActivationDiscardAfterAnotherDiscard() {
        harness.addToBattlefield(player1, new Pyromancy());
        harness.setHand(player1, List.of(new FleetingImage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.setHand(player2, List.of(new Miscalculation()));
        harness.setLibrary(player2, List.of(new GrimMonolith()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);
        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Fleeting Image");
        harness.assertInGraveyard(player2, "Miscalculation");
    }
}
