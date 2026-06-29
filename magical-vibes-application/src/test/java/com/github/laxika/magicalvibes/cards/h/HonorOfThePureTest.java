package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HonorOfThePureTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Honor of the Pure has correct card properties")
    void hasCorrectProperties() {
        HonorOfThePure card = new HonorOfThePure();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Honor of the Pure");
    }

    @Test
    @DisplayName("Resolving puts Honor of the Pure onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Honor of the Pure"));
    }

    // ===== Static effect: buffs own white creatures =====

    @Test
    @DisplayName("Own white creatures get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();

        // Elite Vanguard is 2/1, with +1/+1 should be 3/2
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    // ===== Static effect: does not buff non-white creatures =====

    @Test
    @DisplayName("Own non-white creatures do not get buffed")
    void doesNotBuffNonWhiteCreatures() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Grizzly Bears is 2/2, should remain 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Static effect: does not buff opponent's white creatures =====

    @Test
    @DisplayName("Opponent's white creatures do not get buffed")
    void doesNotBuffOpponentWhiteCreatures() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent opponentVanguard = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();

        // Opponent's white creature should NOT be buffed (OWN_CREATURES scope)
        assertThat(gqs.getEffectivePower(gd, opponentVanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVanguard)).isEqualTo(1);
    }

    // ===== Multiple sources stack =====

    @Test
    @DisplayName("Two Honor of the Pure give +2/+2 to white creatures")
    void twoHonorsStack() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();

        // 2/1 base + 2/2 from two Honors = 4/3
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    // ===== Bonus removed when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Honor of the Pure leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Honor of the Pure"));

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }

    // ===== Bonus applies when Honor resolves =====

    @Test
    @DisplayName("Bonus applies when Honor of the Pure resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();

        // Before casting, no bonus
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        // After resolving, white creature buffed
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();

        // Add a temporary spell boost
        vanguard.setPowerModifier(vanguard.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(6); // 2 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        vanguard.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }
}
