package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FaerieConclaveTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Faerie Conclave has correct card properties")
    void hasCorrectProperties() {
        FaerieConclave card = new FaerieConclave();

        assertThat(card.isEntersTapped()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateLandEffect.class);
    }

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Faerie Conclave enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new FaerieConclave()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent conclave = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Faerie Conclave"))
                .findFirst().orElseThrow();
        assertThat(conclave.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Faerie Conclave produces blue mana")
    void tappingProducesBlueMana() {
        Permanent conclave = addConclaveReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(conclave);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent conclave = addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Faerie Conclave");
        assertThat(entry.getTargetPermanentId()).isEqualTo(conclave.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 2/1 creature with flying")
    void resolvingAbilityMakesItA2x1WithFlying() {
        Permanent conclave = addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(conclave.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(conclave.getAnimatedPower()).isEqualTo(2);
        assertThat(conclave.getAnimatedToughness()).isEqualTo(1);
        assertThat(gqs.isCreature(gd, conclave)).isTrue();
        assertThat(gqs.getEffectivePower(gd, conclave)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, conclave)).isEqualTo(1);
        assertThat(conclave.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Animated Faerie Conclave gains Faerie subtype")
    void animatedGainsFaerieSubtype() {
        Permanent conclave = addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(conclave.getTransientSubtypes()).containsExactly(CardSubtype.FAERIE);
    }

    @Test
    @DisplayName("Animated Faerie Conclave becomes blue")
    void animatedBecomesBlue() {
        Permanent conclave = addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(conclave.getAnimatedColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Faerie Conclave is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent conclave = addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(conclave.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(gqs.isCreature(gd, conclave)).isTrue();
    }

    // ===== End of turn resets animation =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent conclave = addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, conclave)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(conclave.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, conclave)).isFalse();
        assertThat(conclave.getGrantedKeywords()).isEmpty();
        assertThat(conclave.getTransientSubtypes()).isEmpty();
        assertThat(conclave.getAnimatedColor()).isNull();
    }

    // ===== Mana cost enforcement =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Ability does not tap =====

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent conclave = addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(conclave.isTapped()).isFalse();
    }

    // ===== Ability fizzles if removed =====

    @Test
    @DisplayName("Ability fizzles if Faerie Conclave is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addConclaveReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Faerie Conclave is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent conclave = addConclaveReady(player1);

        assertThat(gqs.isCreature(gd, conclave)).isFalse();
        assertThat(conclave.getCard().getType()).isEqualTo(CardType.LAND);
    }

    // ===== Helper methods =====

    private Permanent addConclaveReady(Player player) {
        FaerieConclave card = new FaerieConclave();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
