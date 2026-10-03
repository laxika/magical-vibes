package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EfficientConstruction;
import com.github.laxika.magicalvibes.cards.m.ManaBloom;
import com.github.laxika.magicalvibes.cards.r.ReadTheBones;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonOfFatesDesign.class, EfficientConstruction.class, ReadTheBones.class, ManaBloom.class})
class DemonOfFatesDesignTest extends BaseCardTest {

    @Test
    @DisplayName("Once each turn, the controller may cast an enchantment by paying its mana value in life")
    void castsEnchantmentByPayingManaValueInLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new EfficientConstruction()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The life alternative is limited to enchantment spells and once each turn")
    void lifeAlternativeIsRestricted() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new EfficientConstruction()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new EfficientConstruction()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new ReadTheBones()));
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing another enchantment boosts power by its mana value until end of turn")
    void sacrificesEnchantmentForManaValueBoost() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonOfFatesDesign());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new EfficientConstruction());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.getPowerModifier()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantment);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(demon.getPowerModifier()).isZero();
    }

    @Test
    void lifeAlternativeCannotCastWithNonzeroX() {
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new ManaBloom()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifeAlternativeCanCastWithZeroX() {
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new ManaBloom()));
        harness.setLife(player1, 20);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Mana Bloom")
                .getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void opponentsDemonDoesNotOfferLifePayment() {
        harness.addToBattlefield(player2, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new EfficientConstruction()));
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void normalManaPaymentDoesNotConsumeLifeAlternative() {
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new EfficientConstruction(), "{3}{U}");
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.setHand(player1, List.of(new EfficientConstruction()));
        harness.castEnchantment(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotPayMoreLifeThanAvailable() {
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setHand(player1, List.of(new EfficientConstruction()));
        harness.setLife(player1, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void separateDemonsEachOfferOneLifePayment() {
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.addToBattlefield(player1, new DemonOfFatesDesign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new EfficientConstruction(), new EfficientConstruction(),
                new EfficientConstruction()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSacrificeSelfOrOpponentsEnchantment() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonOfFatesDesign());
        Permanent opposingEnchantment = harness.addToBattlefieldAndReturn(player2, new EfficientConstruction());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(demon);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingEnchantment);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeIsPaidBeforeBoostResolves() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DemonOfFatesDesign());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new EfficientConstruction());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantment);
        assertThat(demon.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(demon.getPowerModifier()).isEqualTo(4);
        assertThat(demon.getToughnessModifier()).isZero();
    }
}
