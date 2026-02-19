package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChimericStaffTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Chimeric Staff has correct card properties")
    void hasCorrectProperties() {
        ChimericStaff card = new ChimericStaff();

        assertThat(card.getName()).isEqualTo("Chimeric Staff");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{4}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isNull();
        assertThat(card.getToughness()).isNull();
        assertThat(card.getSubtypes()).isEmpty();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{X}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateSelfEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Chimeric Staff puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ChimericStaff()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chimeric Staff");
    }

    @Test
    @DisplayName("Resolving Chimeric Staff puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new ChimericStaff()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Chimeric Staff"));
    }

    // ===== Activate ability — basic animation =====

    @Test
    @DisplayName("Activating ability puts AnimateSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Chimeric Staff");
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetPermanentId()).isEqualTo(staffPerm.getId());
    }

    @Test
    @DisplayName("Resolving ability with X=3 makes it a 3/3 creature")
    void resolvingAbilityWithX3MakesItA3x3() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(staffPerm.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(staffPerm.getAnimatedPower()).isEqualTo(3);
        assertThat(staffPerm.getAnimatedToughness()).isEqualTo(3);
        assertThat(gqs.isCreature(gd, staffPerm)).isTrue();
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, staffPerm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving ability with X=5 makes it a 5/5 creature")
    void resolvingAbilityWithX5MakesItA5x5() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, staffPerm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating ability with X=0 is legal and costs no mana")
    void activatingWithX0IsLegal() {
        addStaffReady(player1);
        // No mana needed for X=0

        harness.activateAbility(player1, 0, 0, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(0);
    }

    // ===== Gains Construct subtype when animated =====

    @Test
    @DisplayName("Gains Construct creature subtype when animated")
    void gainsConstructSubtypeWhenAnimated() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        // Before animation: no subtypes
        assertThat(staffPerm.getCard().getSubtypes()).isEmpty();
        assertThat(staffPerm.getGrantedSubtypes()).isEmpty();

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        // After animation: gains Construct
        assertThat(staffPerm.getGrantedSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
    }

    @Test
    @DisplayName("Construct subtype is cleared at end of turn")
    void constructSubtypeClearedAtEndOfTurn() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();
        assertThat(staffPerm.getGrantedSubtypes()).containsExactly(CardSubtype.CONSTRUCT);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(staffPerm.getGrantedSubtypes()).isEmpty();
    }

    // ===== Does not tap =====

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);

        assertThat(staffPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent staffPerm = addStaffReady(player1);
        staffPerm.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Re-activation replaces P/T =====

    @Test
    @DisplayName("Re-activating with different X replaces P/T instead of stacking")
    void reactivatingReplacesInsteadOfStacking() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 8);

        // First activation: X=3
        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(3);

        // Second activation: X=5 — should replace, not add
        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, staffPerm)).isEqualTo(5);
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 3, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Chimeric Staff is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent staffPerm = addStaffReady(player1);

        assertThat(gqs.isCreature(gd, staffPerm)).isFalse();
        assertThat(staffPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    // ===== End of turn resets animation =====

    @Test
    @DisplayName("Animation resets at end of turn — reverts to non-creature artifact")
    void animationResetsAtEndOfTurn() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, staffPerm)).isTrue();
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(3);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(staffPerm.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, staffPerm)).isFalse();
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, staffPerm)).isEqualTo(0);
    }

    // ===== Ability fizzles if removed =====

    @Test
    @DisplayName("Ability fizzles if Chimeric Staff is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);

        // Remove Chimeric Staff before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== No summoning sickness restriction (not a tap ability) =====

    @Test
    @DisplayName("Can activate ability with summoning sickness since it does not tap")
    void canActivateWithSummoningSickness() {
        ChimericStaff card = new ChimericStaff();
        Permanent staffPerm = new Permanent(card);
        // summoningSick is true by default
        gd.playerBattlefields.get(player1.getId()).add(staffPerm);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Interaction with static effects =====

    @Test
    @DisplayName("Self-animated Staff benefits from Glorious Anthem")
    void selfAnimatedBenefitsFromGloriousAnthem() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        // 3/3 base from animation + 1/1 from Glorious Anthem = 4/4
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, staffPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Self-animated Staff with March of the Machines uses X as base, not mana value")
    void selfAnimatedWithMarchUsesSelfAnimationBase() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        // Self-animation sets base to X=2, March should NOT also add mana value (4) on top
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, staffPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("March of the Machines animates Staff when not self-animated")
    void marchAnimatesStaffWhenNotSelfAnimated() {
        Permanent staffPerm = addStaffReady(player1);
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        // Not self-animated — March should animate it with P/T = mana value = 4
        assertThat(gqs.isCreature(gd, staffPerm)).isTrue();
        assertThat(gqs.getEffectivePower(gd, staffPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, staffPerm)).isEqualTo(4);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("activates Chimeric Staff's ability"));
    }

    @Test
    @DisplayName("Resolving ability logs the animation")
    void resolvingAbilityLogsAnimation() {
        addStaffReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("becomes a 3/3 artifact creature"));
    }

    // ===== Helper methods =====

    private Permanent addStaffReady(Player player) {
        ChimericStaff card = new ChimericStaff();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
