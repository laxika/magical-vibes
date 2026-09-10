package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieConclave.class})
class FaerieConclaveTest extends BaseCardTest {

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Faerie Conclave enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new FaerieConclave()));

        harness.playLand(player1, 0);

        Permanent conclave = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(conclave.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Faerie Conclave produces blue mana")
    void tappingProducesBlueMana() {
        addCreatureReady(player1, new FaerieConclave());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(conclave.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 2/1 creature with flying")
    void resolvingAbilityMakesItA2x1WithFlying() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, conclave)).isTrue();
        assertThat(gqs.getEffectivePower(gd, conclave)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, conclave)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, conclave, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Animated Faerie Conclave gains Faerie subtype")
    void animatedGainsFaerieSubtype() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, conclave)).contains(CardSubtype.FAERIE);
    }

    @Test
    @DisplayName("Animated Faerie Conclave becomes blue")
    void animatedBecomesBlue() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, conclave)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Faerie Conclave is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, conclave)).isTrue();
        assertThat(gqs.isCreature(gd, conclave)).isTrue();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== End of turn resets animation =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, conclave)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, conclave)).isFalse();
        assertThat(gqs.isLand(gd, conclave)).isTrue();
        assertThat(gqs.hasKeyword(gd, conclave, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, conclave)).doesNotContain(CardSubtype.FAERIE);
        assertThat(gqs.getEffectiveColors(gd, conclave)).isEmpty();
    }

    // ===== Mana cost enforcement =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability requires blue mana")
    void abilityRequiresBlueMana() {
        addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability does not tap =====

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(conclave.isTapped()).isFalse();
    }

    // ===== Ability fizzles if removed =====

    @Test
    @DisplayName("Ability fizzles if Faerie Conclave is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(conclave);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Faerie Conclave is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent conclave = addCreatureReady(player1, new FaerieConclave());

        assertThat(gqs.isCreature(gd, conclave)).isFalse();
        assertThat(gqs.isLand(gd, conclave)).isTrue();
    }

}
