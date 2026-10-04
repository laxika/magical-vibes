package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HopeTender.class, Forest.class, FrilledSandwalla.class})
class HopeTenderTest extends BaseCardTest {

    @Test
    @DisplayName("First ability untaps a tapped land and does not exert")
    void firstAbilityUntapsLandWithoutExert() {
        Permanent tender = addReadyTender(player1);
        Permanent forest = addForest(player1);
        forest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(tender.isTapped()).isTrue();
        assertThat(tender.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("First ability cannot target a non-land")
    void firstAbilityCannotTargetCreature() {
        addReadyTender(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Second ability untaps two tapped lands and exerts")
    void secondAbilityUntapsTwoLandsAndExerts() {
        Permanent tender = addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
        assertThat(tender.isTapped()).isTrue();
        assertThat(tender.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can target opponent's lands")
    void secondAbilityCanTargetOpponentsLands() {
        addReadyTender(player1);
        Permanent ownForest = addForest(player1);
        Permanent oppForest = addForest(player2);
        ownForest.tap();
        oppForest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(ownForest.getId(), oppForest.getId()));
        harness.passBothPriorities();

        assertThat(ownForest.isTapped()).isFalse();
        assertThat(oppForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability cannot target a creature as either land")
    void secondAbilityCannotTargetCreature() {
        addReadyTender(player1);
        Permanent forest = addForest(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrilledSandwalla());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(forest.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exertIsPaidBeforeAbilityResolves() {
        Permanent tender = addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));

        assertThat(tender.isTapped()).isTrue();
        assertThat(tender.getSkipUntapCount()).isEqualTo(1);
        assertThat(forest1.isTapped()).isTrue();
        assertThat(forest2.isTapped()).isTrue();
    }

    @Test
    void exertStillAppliesWhenBothTargetsLeaveBattlefield() {
        Permanent tender = addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(forest1, forest2));
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(tender.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(tender.isTapped()).isFalse();
    }

    @Test
    void exertDoesNotPreventUntappingUnderAnotherController() {
        Permanent tender = addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(tender);
        gd.playerBattlefields.get(player2.getId()).add(tender);

        harness.performUntapStep(player2);

        assertThat(tender.isTapped()).isFalse();
    }

    @Test
    void exertExpiresDuringExertingPlayersUntapEvenAfterControlChanges() {
        Permanent tender = addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(tender);
        gd.playerBattlefields.get(player2.getId()).add(tender);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(tender.isTapped()).isTrue();
        assertThat(tender.getSkipUntapCount()).isZero();
        harness.performUntapStep(player2);
        assertThat(tender.isTapped()).isFalse();
    }

    @Test
    void secondAbilityRequiresTwoDistinctLands() {
        Permanent tender = addReadyTender(player1);
        Permanent forest = addForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(forest.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tender.isTapped()).isFalse();
        assertThat(tender.getSkipUntapCount()).isZero();
    }

    @Test
    void secondAbilityUntapsRemainingLegalTarget() {
        addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(forest1);

        harness.passBothPriorities();

        assertThat(forest2.isTapped()).isFalse();
    }

    @Test
    void exertSkipsOnlyNextUntapStep() {
        Permanent tender = addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(tender.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(tender.isTapped()).isFalse();
    }

    private Permanent addReadyTender(Player player) {
        return addCreatureReady(player, new HopeTender());
    }

    private Permanent addForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
