package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HierophantsChaliceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Hierophant's Chalice needs a target")
    void needsTarget() {
        HierophantsChalice card = new HierophantsChalice();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
    }

    @Test
    @DisplayName("Has ETB drain life effect with correct amounts")
    void hasEtbEffect() {
        HierophantsChalice card = new HierophantsChalice();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class);
        TargetPlayerLosesLifeAndControllerGainsLifeEffect effect =
                (TargetPlayerLosesLifeAndControllerGainsLifeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.lifeLoss()).isEqualTo(1);
        assertThat(effect.lifeGain()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has activated mana ability that produces colorless mana")
    void hasManaAbility() {
        HierophantsChalice card = new HierophantsChalice();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(AwardManaEffect.class);
        AwardManaEffect manaEffect = (AwardManaEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(manaEffect.color()).isEqualTo(ManaColor.COLORLESS);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as an artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HierophantsChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hierophant's Chalice");
    }

    // ===== Resolving artifact spell =====

    @Test
    @DisplayName("Resolving puts Hierophant's Chalice on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castChalice();
        harness.passBothPriorities(); // resolve artifact spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hierophant's Chalice"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hierophant's Chalice");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    // ===== ETB life drain =====

    @Test
    @DisplayName("ETB trigger causes target opponent to lose 1 life and controller to gain 1 life")
    void etbDrainsLife() {
        castChalice();
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castChalice();
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castChalice();
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds one colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new HierophantsChalice());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Helpers =====

    private void castChalice() {
        harness.setHand(player1, List.of(new HierophantsChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0, player2.getId());
    }
}
