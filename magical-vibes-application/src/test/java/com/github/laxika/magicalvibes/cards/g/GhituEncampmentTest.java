package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed(GhituEncampment.class)
class GhituEncampmentTest extends BaseCardTest {

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Ghitu Encampment enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new GhituEncampment()));

        harness.playLand(player1, 0);

        Permanent encampment = findPermanent(player1, "Ghitu Encampment");
        assertThat(encampment.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Ghitu Encampment produces red mana")
    void tappingProducesRedMana() {
        addCreatureReady(player1, new GhituEncampment());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating Ghitu Encampment's ability puts an activated ability on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(encampment.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 2/1 creature with first strike")
    void resolvingAbilityMakesItA2x1WithFirstStrike() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, encampment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, encampment)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, encampment)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, encampment, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Animated Ghitu Encampment gains Warrior subtype")
    void animatedGainsWarriorSubtype() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, encampment)).containsExactly(CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Animated Ghitu Encampment becomes red")
    void animatedBecomesRed() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, encampment)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Ghitu Encampment is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, encampment)).isTrue();
        assertThat(gqs.isCreature(gd, encampment)).isTrue();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    // ===== End of turn resets animation =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, encampment)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, encampment)).isFalse();
        assertThat(gqs.isLand(gd, encampment)).isTrue();
        assertThat(gqs.hasKeyword(gd, encampment, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, encampment)).doesNotContain(CardSubtype.WARRIOR);
        assertThat(gqs.getEffectiveColors(gd, encampment)).isEmpty();
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Ghitu Encampment is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());

        assertThat(gqs.isCreature(gd, encampment)).isFalse();
    }

    @Test
    @DisplayName("Activating ability consumes the {1}{R} cost")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Ability requires red mana")
    void abilityRequiresRedMana() {
        addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating ability does not tap Ghitu Encampment")
    void activatingAbilityDoesNotTap() {
        Permanent encampment = addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(encampment.isTapped()).isFalse();
    }

    // ===== Ability fizzles if removed =====

    @Test
    @DisplayName("Ability fizzles if Ghitu Encampment is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCreatureReady(player1, new GhituEncampment());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
