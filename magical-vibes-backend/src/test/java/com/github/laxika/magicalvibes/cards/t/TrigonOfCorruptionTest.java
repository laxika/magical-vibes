package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrigonOfCorruptionTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 3 charge counters")
    void hasEnterWithChargeCountersEffect() {
        TrigonOfCorruption card = new TrigonOfCorruption();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect =
                (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has two activated abilities: add charge counter and put -1/-1 counter")
    void hasTwoActivatedAbilities() {
        TrigonOfCorruption card = new TrigonOfCorruption();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {B}{B}, {T} to put a charge counter
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{B}{B}");
        assertThat(card.getActivatedAbilities().get(0).getEffects())
                .hasSize(1)
                .anyMatch(e -> e instanceof PutChargeCounterOnSelfEffect);

        // Second ability: {2}, {T}, remove a charge counter to put -1/-1 counter on target creature
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(1).getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof PutMinusOneMinusOneCounterOnTargetCreatureEffect);
    }

    // ===== ETB: enters with 3 charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TrigonOfCorruption()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Corruption"))
                .findFirst().orElseThrow();
        assertThat(trigon.getChargeCounters()).isEqualTo(3);
    }

    // ===== First ability: {B}{B}, {T} to add charge counter =====

    @Test
    @DisplayName("First ability adds a charge counter when paying {B}{B}")
    void firstAbilityAddsChargeCounter() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Corruption"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(4);
    }

    @Test
    @DisplayName("First ability cannot be activated without {B}{B} mana")
    void firstAbilityRequiresBlackMana() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());

        // No mana available
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Second ability: {2}, {T}, remove charge counter to put -1/-1 counter =====

    @Test
    @DisplayName("Second ability puts a -1/-1 counter on target creature")
    void secondAbilityPutsMinusCounter() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Corruption"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, bearsId);
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(trigon.getChargeCounters()).isEqualTo(0);

        // -1/-1 counter placed
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability cannot be activated with 0 charge counters")
    void cannotActivateSecondAbilityWithNoCounters() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Corruption"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability kills a 1/1 creature via -1/-1 counter")
    void secondAbilityKillsOneOneCreature() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());

        // Create a 1/1 by giving Grizzly Bears (2/2) a -1/-1 counter
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        UUID bearsId = bears.getId();

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Corruption"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, bearsId);
        harness.passBothPriorities();

        // Bears (1/1) got another -1/-1 counter making it 0/0, dies to SBA
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Second ability fizzles when target creature is removed before resolution")
    void secondAbilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Corruption"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, bearsId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Counters are still removed (cost is paid on activation)
        assertThat(trigon.getChargeCounters()).isEqualTo(0);

        // Ability fizzles
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Tap constraint =====

    @Test
    @DisplayName("Cannot use both abilities in the same turn since both require tapping")
    void cannotUseBothAbilitiesSameTurn() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Corruption"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        // Use first ability (tap to add counter)
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Now trigon is tapped — cannot activate second ability
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
