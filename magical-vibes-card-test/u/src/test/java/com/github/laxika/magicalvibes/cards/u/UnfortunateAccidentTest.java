package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({UnfortunateAccident.class, GrizzlyBears.class})
class UnfortunateAccidentTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys a target creature")
    void destroysTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), 2, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The token mode creates a Mercenary with its sorcery-speed boost ability")
    void createsMercenaryWithBoostAbility() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        cast(new int[]{1}, List.of(), 1, 1);

        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Spree resolves both modes and charges both additional costs")
    void resolvesBothModes() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(target.getId()), 2, 3);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The destroy mode rejects a player target")
    void rejectsNonCreatureTarget() {
        assertThatThrownBy(() -> cast(new int[]{0}, List.of(player2.getId()), 2, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenModeWorksWithoutAnyCreaturesAndPaysOnlyItsAdditionalCost() {
        cast(new int[]{1}, List.of(), 1, 1);

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
        assertThat(mercenary.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(mercenary.getCard().getSubtypes()).containsExactly(CardSubtype.MERCENARY);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertInGraveyard(player1, "Unfortunate Accident");
    }

    @Test
    void tokenModeRequiresItsAdditionalMana() {
        assertThatThrownBy(() -> cast(new int[]{1}, List.of(), 1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mercenary")).isEmpty();
        harness.assertInHand(player1, "Unfortunate Accident");
    }

    @Test
    void destroyModeRequiresAnAdditionalBlackMana() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(target.getId()), 1, 3))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Unfortunate Accident");
    }

    @Test
    void bothModesDoNotCreateATokenWhenTheOnlyTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnfortunateAccident(), new UnfortunateAccident()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(target.getId()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(target.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mercenary")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyCreatedMercenaryCannotActivateItsTapAbility() {
        cast(new int[]{1}, List.of(), 1, 1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotBoostAnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        cast(new int[]{1}, List.of(), 1, 1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotActivateOutsideAMainPhase() {
        cast(new int[]{1}, List.of(), 1, 1);
        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int blackMana, int colorlessMana) {
        harness.setHand(player1, List.of(new UnfortunateAccident()));
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }
}
