package com.github.laxika.magicalvibes.cards.i;

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

import static org.assertj.core.api.Assertions.assertThat;

class InkmothNexusTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Inkmoth Nexus does not enter tapped")
    void doesNotEnterTapped() {
        InkmothNexus card = new InkmothNexus();
        assertThat(card.isEntersTapped()).isFalse();
    }

    @Test
    @DisplayName("Inkmoth Nexus has one activated ability costing {1}")
    void hasCorrectAbility() {
        InkmothNexus card = new InkmothNexus();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateLandEffect.class);
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Inkmoth Nexus produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent nexus = addNexusReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(nexus);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent nexus = addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Inkmoth Nexus");
        assertThat(entry.getTargetPermanentId()).isEqualTo(nexus.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 1/1 artifact creature with flying and infect")
    void resolvingAbilityMakesItAnimated() {
        Permanent nexus = addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(nexus.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(nexus.getAnimatedPower()).isEqualTo(1);
        assertThat(nexus.getAnimatedToughness()).isEqualTo(1);
        assertThat(gqs.isCreature(gd, nexus)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nexus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nexus)).isEqualTo(1);
        assertThat(nexus.getAnimatedColor()).isNull();
        assertThat(nexus.getGrantedSubtypes()).containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.BLINKMOTH);
        assertThat(nexus.getGrantedKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.INFECT);
        assertThat(gqs.isArtifact(nexus)).isTrue();
    }

    @Test
    @DisplayName("Inkmoth Nexus is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent nexus = addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(nexus.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(gqs.isCreature(gd, nexus)).isTrue();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent nexus = addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(nexus.isTapped()).isFalse();
    }

    // ===== Animation resets at end of turn =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent nexus = addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, nexus)).isTrue();
        assertThat(gqs.isArtifact(nexus)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nexus.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, nexus)).isFalse();
        assertThat(gqs.isArtifact(nexus)).isFalse();
        assertThat(nexus.getGrantedKeywords()).isEmpty();
        assertThat(nexus.getGrantedSubtypes()).isEmpty();
    }

    // ===== Mana cost enforcement =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addNexusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Inkmoth Nexus is not a creature or artifact before activation")
    void notACreatureOrArtifactBeforeActivation() {
        Permanent nexus = addNexusReady(player1);

        assertThat(gqs.isCreature(gd, nexus)).isFalse();
        assertThat(nexus.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(gqs.isArtifact(nexus)).isFalse();
    }

    // ===== Helper methods =====

    private Permanent addNexusReady(Player player) {
        InkmothNexus card = new InkmothNexus();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
