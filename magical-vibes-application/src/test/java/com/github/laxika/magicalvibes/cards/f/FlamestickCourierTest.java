package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamestickCourier.class, GoblinSkyRaider.class, GlorySeeker.class, Forest.class})
class FlamestickCourierTest extends BaseCardTest {

    @Test
    @DisplayName("The ability gives a Goblin +2/+2 and haste while Flamestick Courier remains tapped")
    void abilityBoostsGoblinWhileCourierRemainsTapped() {
        Permanent courier = addCreatureReady(player1, new FlamestickCourier());
        Permanent goblin = addCreatureReady(player1, new GoblinSkyRaider());
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste persist past the end of turn while Flamestick Courier stays tapped")
    void boostAndHastePersistPastEndOfTurn() {
        Permanent courier = addCreatureReady(player1, new FlamestickCourier());
        Permanent goblin = addCreatureReady(player1, new GoblinSkyRaider());
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste end when Flamestick Courier untaps")
    void boostAndHasteEndWhenCourierUntaps() {
        Permanent courier = addCreatureReady(player1, new FlamestickCourier());
        Permanent goblin = addCreatureReady(player1, new GoblinSkyRaider());
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(courier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Flamestick Courier may remain tapped during its controller's untap step")
    void courierCanRemainTapped() {
        Permanent courier = addCreatureReady(player1, new FlamestickCourier());
        Permanent goblin = addCreatureReady(player1, new GoblinSkyRaider());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-Goblin creature")
    void cannotTargetNonGoblinCreature() {
        addCreatureReady(player1, new FlamestickCourier());
        Permanent creature = addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Goblin creature");
    }

    @Test
    @DisplayName("The ability can target an opponent's Goblin creature")
    void abilityBoostsOpponentsGoblin() {
        Permanent courier = addCreatureReady(player1, new FlamestickCourier());
        Permanent goblin = addCreatureReady(player2, new GoblinSkyRaider());
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new FlamestickCourier());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The boost is not created if Flamestick Courier leaves before the ability resolves")
    void boostDoesNotApplyIfCourierLeavesBeforeResolution() {
        Permanent courier = addCreatureReady(player1, new FlamestickCourier());
        Permanent goblin = addCreatureReady(player1, new GoblinSkyRaider());
        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, courier));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The boost does not resume if Flamestick Courier is tapped again after untapping")
    void boostDoesNotResumeAfterCourierIsRetapped() {
        Permanent courier = addCreatureReady(player1, new FlamestickCourier());
        Permanent firstGoblin = addCreatureReady(player1, new GoblinSkyRaider());
        Permanent secondGoblin = addCreatureReady(player1, new GoblinSkyRaider());
        int firstBasePower = gqs.getEffectivePower(gd, firstGoblin);
        int firstBaseToughness = gqs.getEffectiveToughness(gd, firstGoblin);
        int secondBasePower = gqs.getEffectivePower(gd, secondGoblin);
        int secondBaseToughness = gqs.getEffectiveToughness(gd, secondGoblin);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, firstGoblin.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(firstBasePower + 2);

        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(courier.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(firstBasePower);
        assertThat(gqs.getEffectiveToughness(gd, firstGoblin)).isEqualTo(firstBaseToughness);
        assertThat(gqs.hasKeyword(gd, firstGoblin, Keyword.HASTE)).isFalse();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, secondGoblin.getId());
        harness.passBothPriorities();

        assertThat(courier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, firstGoblin)).isEqualTo(firstBasePower);
        assertThat(gqs.getEffectiveToughness(gd, firstGoblin)).isEqualTo(firstBaseToughness);
        assertThat(gqs.getEffectivePower(gd, secondGoblin)).isEqualTo(secondBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, secondGoblin)).isEqualTo(secondBaseToughness + 2);
        assertThat(gqs.hasKeyword(gd, firstGoblin, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondGoblin, Keyword.HASTE)).isTrue();
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
