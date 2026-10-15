package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MothriderCavalry.class, GrizzlyBears.class, Shock.class})
class MothriderCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Costs two less when there are no other creature cards in hand")
    void costsLessWithNoOtherCreatureCards() {
        harness.setHand(player1, List.of(new MothriderCavalry(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs two less when the only other creature cards are named Mothrider Cavalry")
    void costsLessWithOnlyOtherMothriderCavalryCards() {
        harness.setHand(player1, List.of(new MothriderCavalry(), new MothriderCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mothrider Cavalry");
    }

    @Test
    @DisplayName("Does not get the reduction when another creature card has a different name")
    void doesNotCostLessWithAnotherCreatureCard() {
        harness.setHand(player1, List.of(new MothriderCavalry(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void buffsOtherCreaturesYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MothriderCavalry());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A differently named creature prevents the discount even alongside another Cavalry")
    void mixedCreatureHandPreventsDiscount() {
        harness.setHand(player1, List.of(new MothriderCavalry(), new MothriderCavalry(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Pays the full cost when a differently named creature is in hand")
    void paysFullCostWithAnotherCreatureCard() {
        harness.setHand(player1, List.of(new MothriderCavalry(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mothrider Cavalry");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The discount does not reduce either white mana requirement")
    void discountDoesNotReduceColoredMana() {
        harness.setHand(player1, List.of(new MothriderCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creatures outside the caster's hand do not prevent the discount")
    void ignoresCreaturesOutsideOwnHand() {
        harness.setHand(player1, List.of(new MothriderCavalry()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Cavalries boost each other and their bonuses stack on other creatures")
    void multipleCavalriesStackBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MothriderCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MothriderCavalry());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The bonus ends when Mothrider Cavalry leaves the battlefield")
    void bonusEndsWhenSourceDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MothriderCavalry());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mothrider Cavalry");
        harness.assertNotOnBattlefield(player1, "Mothrider Cavalry");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
