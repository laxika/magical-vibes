package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhituEncampmentTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Ghitu Encampment has correct card properties")
    void hasCorrectProperties() {
        GhituEncampment card = new GhituEncampment();

        assertThat(card.isEntersTapped()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateLandEffect.class);
    }

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Ghitu Encampment enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new GhituEncampment()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent encampment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ghitu Encampment"))
                .findFirst().orElseThrow();
        assertThat(encampment.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Ghitu Encampment produces red mana")
    void tappingProducesRedMana() {
        Permanent encampment = addEncampmentReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(encampment);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent encampment = addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ghitu Encampment");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(encampment.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 2/1 creature with first strike")
    void resolvingAbilityMakesItA2x1WithFirstStrike() {
        Permanent encampment = addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(encampment.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, encampment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, encampment)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, encampment)).isEqualTo(1);
        assertThat(encampment.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Animated Ghitu Encampment gains Warrior subtype")
    void animatedGainsWarriorSubtype() {
        Permanent encampment = addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(encampment.getTransientSubtypes()).containsExactly(CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Animated Ghitu Encampment becomes red")
    void animatedBecomesRed() {
        Permanent encampment = addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(encampment.getAnimatedColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Ghitu Encampment is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent encampment = addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(encampment.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(gqs.isCreature(gd, encampment)).isTrue();
    }

    // ===== End of turn resets animation =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent encampment = addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, encampment)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(encampment.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, encampment)).isFalse();
        assertThat(encampment.getGrantedKeywords()).isEmpty();
        assertThat(encampment.getTransientSubtypes()).isEmpty();
        assertThat(encampment.getAnimatedColor()).isNull();
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Ghitu Encampment is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent encampment = addEncampmentReady(player1);

        assertThat(gqs.isCreature(gd, encampment)).isFalse();
    }

    // ===== Ability fizzles if removed =====

    @Test
    @DisplayName("Ability fizzles if Ghitu Encampment is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addEncampmentReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addEncampmentReady(Player player) {
        GhituEncampment card = new GhituEncampment();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
