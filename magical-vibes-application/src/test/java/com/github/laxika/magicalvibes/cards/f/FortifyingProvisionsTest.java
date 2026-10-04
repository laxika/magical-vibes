package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DanceOfTheManse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FortifyingProvisions.class, Flutterfox.class, DanceOfTheManse.class})
class FortifyingProvisionsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new FortifyingProvisions());
        harness.addToBattlefield(player1, new Flutterfox());
        harness.addToBattlefield(player2, new Flutterfox());

        Permanent ownFox = findPermanent(player1, "Flutterfox");
        Permanent opposingFox = findPermanent(player2, "Flutterfox");

        assertThat(gqs.getEffectivePower(gd, ownFox)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownFox)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingFox)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingFox)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with a Food token")
    void entersWithFoodToken() {
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(food.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Food created by Fortifying Provisions can be sacrificed for 3 life")
    void foodCanBeSacrificedForLife() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Fortifying Provisions boosts itself when Dance of the Manse makes it a creature")
    void boostsItselfWhenAnimated() {
        FortifyingProvisions provisions = new FortifyingProvisions();
        harness.setGraveyard(player1, List.of(provisions));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);
        harness.handleMultipleCardsChosen(player1, List.of(provisions.getId()));
        resolveAllTriggers();

        Permanent animated = findPermanent(player1, "Fortifying Provisions");
        assertThat(gqs.isCreature(gd, animated)).isTrue();
        assertThat(gqs.getEffectivePower(gd, animated)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, animated)).isEqualTo(5);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Fortifying Provisions boost creatures already on the battlefield")
    void multipleCopiesStackOnExistingCreatures() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new Flutterfox());
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, fox)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, fox)).isEqualTo(4);
        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and grants life only when its ability resolves")
    void foodSacrificeIsPaidBeforeLifeGain() {
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Food cannot be sacrificed for life without paying two mana")
    void foodRequiresTwoMana() {
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped Food cannot activate its life-gain ability")
    void foodRequiresTapCost() {
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        findPermanent(player1, "Food").setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
