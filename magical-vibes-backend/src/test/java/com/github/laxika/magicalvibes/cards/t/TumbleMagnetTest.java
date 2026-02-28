package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TumbleMagnetTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 3 charge counters")
    void hasEnterWithChargeCountersEffect() {
        TumbleMagnet card = new TumbleMagnet();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has one activated ability with tap, counter removal cost, and tap effect")
    void hasActivatedAbilityStructure() {
        TumbleMagnet card = new TumbleMagnet();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof TapTargetPermanentEffect);
        assertThat(ability.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter targetFilter = (PermanentPredicateTargetFilter) ability.getTargetFilter();
        assertThat(targetFilter.predicate()).isInstanceOf(PermanentAnyOfPredicate.class);
    }

    // ===== Entering the battlefield with charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TumbleMagnet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent magnet = findMagnet(player1);
        assertThat(magnet.getChargeCounters()).isEqualTo(3);
    }

    // ===== Activating ability: targeting creatures =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingTargetingCreaturePutsOnStack() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Tumble Magnet");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Tumble Magnet")
    void activatingTapsMagnet() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(magnet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyMagnet(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability removes a charge counter")
    void resolvingRemovesChargeCounter() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(magnet.getChargeCounters()).isEqualTo(2);
    }

    // ===== Activating ability: targeting artifacts =====

    @Test
    @DisplayName("Can tap target artifact")
    void canTapTargetArtifact() {
        addReadyMagnet(player1);
        Permanent targetArtifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isTrue();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyMagnet(player1);
        Permanent enchantment = addReadyEnchantment(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    // ===== Charge counter depletion =====

    @Test
    @DisplayName("Can activate multiple times with enough counters (untapping between)")
    void canActivateMultipleTimes() {
        Permanent magnet = addReadyMagnet(player1);
        Permanent target = addReadyCreature(player2);

        // First activation
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        magnet.untap();
        target.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        magnet.untap();
        target.untap();

        // Third activation
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(magnet.getChargeCounters()).isEqualTo(0);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent magnet = addReadyMagnet(player1);
        magnet.setChargeCounters(0);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent magnet = addReadyMagnet(player1);
        magnet.tap();
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Can target own permanents =====

    @Test
    @DisplayName("Can tap own creature")
    void canTapOwnCreature() {
        addReadyMagnet(player1);
        Permanent ownCreature = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyMagnet(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyMagnet(Player player) {
        TumbleMagnet card = new TumbleMagnet();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setChargeCounters(3);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findMagnet(Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tumble Magnet"))
                .findFirst().orElseThrow();
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        AngelsFeather card = new AngelsFeather();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        Pacifism card = new Pacifism();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
