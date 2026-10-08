package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CollectiveRestraint;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.m.MeddlingMage;
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

@CardUsed({WaxWane.class, KavuTitan.class, CollectiveRestraint.class, MeddlingMage.class})
class WaxWaneTest extends BaseCardTest {

    @Test
    @DisplayName("Wax gives a target creature +2/+2 until end of turn")
    void waxBoostsCreatureUntilEndOfTurn() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, kavu.getId());
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(2);
        assertThat(kavu.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isZero();
        assertThat(kavu.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Wax can target an opponent's creature")
    void waxBoostsOpponentsCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, kavu.getId());
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(2);
        assertThat(kavu.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Wane destroys the targeted enchantment")
    void waneDestroysEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Collective Restraint");
        harness.assertInGraveyard(player2, "Collective Restraint");
    }

    @Test
    @DisplayName("Wane can target your own enchantment")
    void waneDestroysYourOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Collective Restraint");
        harness.assertInGraveyard(player1, "Collective Restraint");
    }

    @Test
    @DisplayName("Wax cannot target a noncreature permanent")
    void waxCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wane cannot target a non-enchantment permanent")
    void waneCannotTargetNonEnchantmentPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void waxCannotBePaidWithWhiteMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void waneCannotBePaidWithGreenMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void waneDoesNotDestroyAnotherEnchantmentWhenItsTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane(), new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
        harness.assertInGraveyard(player2, "Collective Restraint");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void waxCannotBeCastWhenMeddlingMageNamesWax() {
        Permanent mage = harness.addToBattlefieldAndReturn(player2, new MeddlingMage());
        mage.setChosenName("Wax");
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void waneCannotBeCastWhenMeddlingMageNamesWane() {
        Permanent mage = harness.addToBattlefieldAndReturn(player2, new MeddlingMage());
        mage.setChosenName("Wane");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
