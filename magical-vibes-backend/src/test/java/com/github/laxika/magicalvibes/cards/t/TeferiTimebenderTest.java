package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkitteringSurveyor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeferiTimebenderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        TeferiTimebender card = new TeferiTimebender();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+2 ability has UntapTargetPermanentEffect with minTargets=0")
    void plusTwoAbilityHasCorrectEffect() {
        TeferiTimebender card = new TeferiTimebender();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(2);
        assertThat(ability.getMinTargets()).isZero();
        assertThat(ability.getMaxTargets()).isEqualTo(1);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(UntapTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("-3 ability has GainLifeEffect(2) and DrawCardEffect(2)")
    void minusThreeAbilityHasCorrectEffects() {
        TeferiTimebender card = new TeferiTimebender();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-3);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) ability.getEffects().get(0)).amount()).isEqualTo(2);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) ability.getEffects().get(1)).amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("-9 ability has ControllerExtraTurnEffect(1)")
    void minusNineAbilityHasCorrectEffect() {
        TeferiTimebender card = new TeferiTimebender();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-9);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(ControllerExtraTurnEffect.class);
        assertThat(((ControllerExtraTurnEffect) ability.getEffects().getFirst()).count()).isEqualTo(1);
    }

    // ===== +2 ability: Untap up to one target artifact or creature =====

    @Test
    @DisplayName("+2 ability untaps target creature")
    void plusTwoUntapsTargetCreature() {
        Permanent teferi = addReadyTeferi(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bear.tap();

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(teferi.getLoyaltyCounters()).isEqualTo(7); // 5 + 2
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+2 ability untaps target artifact")
    void plusTwoUntapsTargetArtifact() {
        Permanent teferi = addReadyTeferi(player1);
        harness.addToBattlefield(player1, new SkitteringSurveyor());

        Permanent surveyor = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Skittering Surveyor"))
                .findFirst().orElseThrow();
        surveyor.tap();
        surveyor.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, surveyor.getId());
        harness.passBothPriorities();

        assertThat(teferi.getLoyaltyCounters()).isEqualTo(7); // 5 + 2
        assertThat(surveyor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+2 ability can be activated without a target (up to zero)")
    void plusTwoCanActivateWithoutTarget() {
        Permanent teferi = addReadyTeferi(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(teferi.getLoyaltyCounters()).isEqualTo(7); // 5 + 2
    }

    // ===== -3 ability: Gain 2 life and draw two cards =====

    @Test
    @DisplayName("-3 ability gains 2 life and draws 2 cards")
    void minusThreeGainsLifeAndDrawsCards() {
        Permanent teferi = addReadyTeferi(player1);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(teferi.getLoyaltyCounters()).isEqualTo(2); // 5 - 3
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    // ===== -9 ability: Take an extra turn =====

    @Test
    @DisplayName("-9 ability grants controller an extra turn")
    void minusNineGrantsExtraTurn() {
        Permanent teferi = addReadyTeferi(player1);
        teferi.setLoyaltyCounters(9);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Teferi should have 0 loyalty (9 - 9) and be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Teferi, Timebender"));
        // Controller should have an extra turn queued
        assertThat(gd.extraTurns).contains(player1.getId());
    }

    // ===== Loyalty restrictions =====

    @Test
    @DisplayName("Cannot activate -9 when loyalty is only 5")
    void cannotActivateMinusNineWithInsufficientLoyalty() {
        addReadyTeferi(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Helpers =====

    private Permanent addReadyTeferi(Player player) {
        TeferiTimebender card = new TeferiTimebender();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(5);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
