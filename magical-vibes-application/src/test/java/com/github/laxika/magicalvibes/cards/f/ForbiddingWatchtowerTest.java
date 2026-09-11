package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ForbiddingWatchtower.class)
class ForbiddingWatchtowerTest extends BaseCardTest {

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Forbidding Watchtower enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new ForbiddingWatchtower()));

        harness.playLand(player1, 0);

        Permanent watchtower = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(watchtower.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Forbidding Watchtower produces white mana")
    void tappingProducesWhiteMana() {
        addCreatureReady(player1, new ForbiddingWatchtower());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(watchtower.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 1/5 creature")
    void resolvingAbilityMakesItA1x5() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, watchtower)).isTrue();
        assertThat(gqs.getEffectivePower(gd, watchtower)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, watchtower)).isEqualTo(5);
    }

    @Test
    @DisplayName("Animated Forbidding Watchtower gains Soldier subtype")
    void animatedGainsSoldierSubtype() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, watchtower)).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Animated Forbidding Watchtower becomes white")
    void animatedBecomesWhite() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, watchtower)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Animated Forbidding Watchtower does not have flying")
    void animatedDoesNotHaveFlying() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, watchtower, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Forbidding Watchtower is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, watchtower)).isTrue();
        assertThat(gqs.isCreature(gd, watchtower)).isTrue();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    // ===== End of turn resets animation =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, watchtower)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, watchtower)).isFalse();
        assertThat(gqs.isLand(gd, watchtower)).isTrue();
        assertThat(gqs.hasKeyword(gd, watchtower, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, watchtower)).doesNotContain(CardSubtype.SOLDIER);
        assertThat(gqs.getEffectiveColors(gd, watchtower)).isEmpty();
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Forbidding Watchtower is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());

        assertThat(gqs.isCreature(gd, watchtower)).isFalse();
        assertThat(gqs.isLand(gd, watchtower)).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Ability requires white mana")
    void abilityRequiresWhiteMana() {
        addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating ability does not tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent watchtower = addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(watchtower.isTapped()).isFalse();
    }

    // ===== Ability fizzles if removed =====

    @Test
    @DisplayName("Ability fizzles if Forbidding Watchtower is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCreatureReady(player1, new ForbiddingWatchtower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
