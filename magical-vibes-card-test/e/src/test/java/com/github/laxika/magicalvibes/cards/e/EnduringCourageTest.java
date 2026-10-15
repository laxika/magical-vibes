package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringCourage.class, GrizzlyBears.class, DoomBlade.class,
        Disenchant.class, OneWithTheStars.class})
class EnduringCourageTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control entering gets +2/+0 and haste")
    void boostsAndHastesEnteringCreature() {
        harness.addToBattlefield(player1, new EnduringCourage());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new EnduringCourage());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Returns from the graveyard as an enchantment and not a creature")
    void returnsAsEnchantmentOnly() {
        Permanent enduring = harness.addToBattlefieldAndReturn(player1, new EnduringCourage());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, enduring.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Enduring Courage");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.ENCHANTMENT);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.isEnchantment(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("Does not return when it dies as a noncreature")
    void doesNotReturnWhenItWasNotACreature() {
        Permanent enduring = harness.addToBattlefieldAndReturn(player1, new EnduringCourage());

        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, enduring.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, enduring)).isFalse();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, enduring.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Enduring Courage");
        harness.assertNotOnBattlefield(player1, "Enduring Courage");
    }

    @Test
    @DisplayName("Its own entry does not boost it or give it haste")
    void doesNotBoostItself() {
        Permanent enduring = harness.enterBattlefieldAndReturn(player1, new EnduringCourage());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, enduring)).isEqualTo(3);
        assertThat(enduring.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opposing creature entering does not trigger the boost")
    void doesNotBoostOpposingCreature() {
        harness.addToBattlefield(player1, new EnduringCourage());
        Permanent opponent = harness.enterBattlefieldAndReturn(player2, new EnduringCourage());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(3);
        assertThat(opponent.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies each boost another entering creature")
    void multipleCopiesBoostTheEnteringCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EnduringCourage());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EnduringCourage());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new EnduringCourage());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
        assertThat(entering.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(first.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(second.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The returned enchantment keeps its enter ability but does not return again")
    void returnedEnchantmentKeepsItsAbilityButDoesNotReturnAgain() {
        Permanent enduring = harness.addToBattlefieldAndReturn(player1, new EnduringCourage());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, enduring.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Enduring Courage");
        assertThat(returned.getId()).isNotEqualTo(enduring.getId());
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new EnduringCourage());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
        assertThat(entering.hasKeyword(Keyword.HASTE)).isTrue();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, returned.getId());

        harness.assertInGraveyard(player1, "Enduring Courage");
        assertThat(gqs.findPermanentById(gd, returned.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, entering.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }
}
