package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaintedTreats.class, AirElemental.class, DarksteelIngot.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, Millstone.class})
class TaintedTreatsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with mana value 4 or less and creates a Food token")
    void destroysLowManaValueCreatureAndCreatesFood() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys a high-mana-value creature without creating a Food token")
    void destroysHighManaValueCreatureWithoutCreatingFood() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(target);

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Can destroy an artifact")
    void destroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());

        cast(target);

        harness.assertInGraveyard(player2, "Millstone");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TaintedTreats()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    void createsFoodForManaValueExactlyFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(target);

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    void createsFoodEvenWhenIndestructiblePreventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        cast(target);

        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        harness.assertNotInGraveyard(player2, "Darksteel Ingot");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    void doesNotCreateFoodWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TaintedTreats(), new TaintedTreats()));
        addMana();
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyOwnCreatureAndCreatesFoodForCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void destroyingFoodCreatesAReplacementFood() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(creature);
        Permanent originalFood = findPermanent(player1, "Food");

        cast(originalFood);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(findPermanent(player1, "Food").getId()).isNotEqualTo(originalFood.getId());
    }

    @Test
    void foodCanBeSacrificedForThreeLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    void tappedFoodCannotActivateItsLifeGainAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new TaintedTreats()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
