package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.FortifyingProvisions;
import com.github.laxika.magicalvibes.cards.g.GarenbrigPaladin;
import com.github.laxika.magicalvibes.cards.y.YgraEaterOfAll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogNaughty.class, CrawWurm.class, FortifyingProvisions.class, GarenbrigPaladin.class, YgraEaterOfAll.class})
class BogNaughtyTest extends BaseCardTest {

    @Test
    void sacrificesFoodToGiveTargetCreatureMinusThreeMinusThreeUntilEndOfTurn() {
        harness.addToBattlefield(player1, new BogNaughty());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void cannotActivateWithoutFoodEvenWithEnoughMana() {
        harness.addToBattlefield(player1, new BogNaughty());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarenbrigPaladin());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bog Naughty");
    }

    @Test
    void opponentsFoodCannotPayTheSacrificeCost() {
        harness.addToBattlefield(player1, new BogNaughty());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarenbrigPaladin());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOwnCreatureAndSacrificesFoodBeforeResolution() {
        harness.addToBattlefield(player1, new BogNaughty());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GarenbrigPaladin());
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    void canActivateTwiceWithoutTappingAndKillThroughToughnessReduction() {
        harness.addToBattlefield(player1, new BogNaughty());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarenbrigPaladin());
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Garenbrig Paladin");
        harness.assertInGraveyard(player2, "Garenbrig Paladin");
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void canSacrificeItselfWhenYgraMakesItFood() {
        harness.addToBattlefield(player1, new BogNaughty());
        harness.addToBattlefield(player2, new YgraEaterOfAll());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarenbrigPaladin());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bog Naughty");
        harness.assertInGraveyard(player1, "Bog Naughty");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new BogNaughty());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarenbrigPaladin());
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        harness.addToBattlefield(player1, new BogNaughty());
        harness.castFromHand(player1, new FortifyingProvisions(), "{2}{W}");
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Fortifying Provisions")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
