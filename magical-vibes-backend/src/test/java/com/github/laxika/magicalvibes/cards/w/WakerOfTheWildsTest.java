package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutXPlusOnePlusOneCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WakerOfTheWildsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Waker of the Wilds has one activated ability with {X}{G}{G} cost")
    void hasActivatedAbility() {
        WakerOfTheWilds card = new WakerOfTheWilds();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{X}{G}{G}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(PutXPlusOnePlusOneCountersOnTargetPermanentEffect.class);
                    assertThat(effects.get(1)).isInstanceOf(AnimateTargetPermanentEffect.class);
                });
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack with correct X value and target")
    void activatingAbilityPutsItOnStack() {
        Permanent waker = addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 4); // {2}{G}{G} → X=2

        harness.activateAbility(player1, 0, 2, land.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Waker of the Wilds");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(land.getId());
    }

    // ===== Resolving ability =====

    @Test
    @DisplayName("Resolving ability puts X +1/+1 counters on target land")
    void resolvingAbilityPutsCountersOnLand() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 5); // {3}{G}{G} → X=3

        harness.activateAbility(player1, 0, 3, land.getId());
        harness.passBothPriorities();

        assertThat(land.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving ability makes land a 0/0 creature permanently")
    void resolvingAbilityAnimatesLandPermanently() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 4); // {2}{G}{G} → X=2

        harness.activateAbility(player1, 0, 2, land.getId());
        harness.passBothPriorities();

        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(land.getPermanentAnimatedPower()).isEqualTo(0);
        assertThat(land.getPermanentAnimatedToughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Animated land has effective P/T equal to the counters placed")
    void animatedLandHasCorrectEffectivePowerToughness() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 5); // {3}{G}{G} → X=3

        harness.activateAbility(player1, 0, 3, land.getId());
        harness.passBothPriorities();

        // 0/0 base + 3 +1/+1 counters = 3/3
        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animated land gains Elemental subtype")
    void animatedLandGainsElementalSubtype() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G}{G} → X=1

        harness.activateAbility(player1, 0, 1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Animated land gains haste")
    void animatedLandGainsHaste() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G}{G} → X=1

        harness.activateAbility(player1, 0, 1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Animated land is still a land")
    void animatedLandIsStillALand() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G}{G} → X=1

        harness.activateAbility(player1, 0, 1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Animated land is a creature")
    void animatedLandIsCreature() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G}{G} → X=1

        harness.activateAbility(player1, 0, 1, land.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Animation persists after resetModifiers (simulates turn change)")
    void animationPersistsAfterReset() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 4); // {2}{G}{G} → X=2

        harness.activateAbility(player1, 0, 2, land.getId());
        harness.passBothPriorities();

        // Simulate end-of-turn cleanup
        land.resetModifiers();

        // Permanent animation and counters should persist
        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(land.getPlusOnePlusOneCounters()).isEqualTo(2);
        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating ability with X=0 animates land with no counters")
    void activatingWithXZeroAnimatesWithNoCounters() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 2); // {0}{G}{G} → X=0

        harness.activateAbility(player1, 0, 0, land.getId());
        harness.passBothPriorities();

        // Land is animated as 0/0 with no counters — will die to SBA
        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(land.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability on already-animated land adds more counters")
    void activatingOnAlreadyAnimatedLandAddsMoreCounters() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G}{G} → X=1

        harness.activateAbility(player1, 0, 1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Activate again with X=2
        harness.addMana(player1, ManaColor.GREEN, 4); // {2}{G}{G} → X=2
        harness.activateAbility(player1, 0, 2, land.getId());
        harness.passBothPriorities();

        // Should have 1 + 2 = 3 counters total
        assertThat(land.getPlusOnePlusOneCounters()).isEqualTo(3);
        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target opponent's land")
    void cannotTargetOpponentsLand() {
        addWaker(player1);
        Permanent opponentLand = addLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not a land")
    void cannotTargetNonLandCreature() {
        addWaker(player1);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent creature = new Permanent(bear);
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(creature);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mana validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana for GG")
    void cannotActivateWithoutEnoughMana() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 1); // Need at least GG

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 5); // {3}{G}{G} → X=3

        harness.activateAbility(player1, 0, 3, land.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Fizzling =====

    @Test
    @DisplayName("Ability fizzles if target land is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addWaker(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 2, land.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(land);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helper methods =====

    private Permanent addWaker(Player player) {
        WakerOfTheWilds card = new WakerOfTheWilds();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLand(Player player) {
        Forest forest = new Forest();
        Permanent perm = new Permanent(forest);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
