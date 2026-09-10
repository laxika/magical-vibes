package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FungusElemental.class, Forest.class, WindingCanyons.class})
class FungusElementalTest extends BaseCardTest {

    private Permanent elemental() {
        return findPermanent(player1, "Fungus Elemental");
    }

    /**
     * Casts the Elemental so it is recorded as having entered the battlefield this turn, with a
     * Forest already in play to feed the sacrifice cost. The Forest ends up at battlefield index 0
     * and the Elemental at index 1.
     */
    private void castElementalWithForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new FungusElemental(), "{3}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrificing a Forest the turn it entered puts a +2/+2 counter on it")
    void putsPlusTwoCounterWhenActivatedTheTurnItEntered() {
        castElementalWithForest();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(elemental().getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
        harness.assertInGraveyard(player1, "Forest");

        harness.passBothPriorities();

        assertThat(elemental().getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elemental())).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elemental())).isEqualTo(5);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate after the turn it entered ends")
    void cannotActivateAfterTheTurnItEnteredEnds() {
        castElementalWithForest();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UNTAP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elemental().getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The counter is permanent — it survives the end of turn")
    void counterSurvivesEndOfTurn() {
        castElementalWithForest();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental().getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate when the creature did not enter the battlefield this turn")
    void cannotActivateWhenItDidNotEnterThisTurn() {
        harness.addToBattlefield(player1, new Forest());
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new FungusElemental());
        perm.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elemental().getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a Forest to sacrifice")
    void cannotActivateWithoutAForest() {
        harness.castFromHand(player1, new FungusElemental(), "{3}{G}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elemental().getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Forest land to the ability")
    void cannotSacrificeNonForestLand() {
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.castFromHand(player1, new FungusElemental(), "{3}{G}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Winding Canyons");
    }
}
