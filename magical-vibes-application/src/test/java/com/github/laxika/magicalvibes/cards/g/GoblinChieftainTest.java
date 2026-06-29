package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinChieftainTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Goblin Chieftain has static boost effect for own Goblins")
    void hasCorrectProperties() {
        GoblinChieftain card = new GoblinChieftain();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).containsExactly(Keyword.HASTE);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isNotNull();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Goblin Chieftain puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GoblinChieftain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Chieftain");
    }

    @Test
    @DisplayName("Resolving puts Goblin Chieftain onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GoblinChieftain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Chieftain"));
    }

    // ===== Static effect: buffs other own Goblins =====

    @Test
    @DisplayName("Other own Goblin creatures get +1/+1 and haste")
    void buffsOtherOwnGoblins() {
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player1, new GoblinChieftain());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Goblin Chieftain does not buff itself with the static effect")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new GoblinChieftain());

        Permanent chieftain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Chieftain"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(2);
        // Chieftain has innate haste from Scryfall, but +1/+1 should not apply to itself
    }

    @Test
    @DisplayName("Does not buff non-Goblin creatures")
    void doesNotBuffNonGoblins() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinChieftain());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Goblin creatures")
    void doesNotBuffOpponentGoblins() {
        harness.addToBattlefield(player1, new GoblinChieftain());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());

        Permanent opponentGoblin = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentGoblin, Keyword.HASTE)).isFalse();
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Goblin Chieftains buff each other")
    void twoChieftainsBuffEachOther() {
        harness.addToBattlefield(player1, new GoblinChieftain());
        harness.addToBattlefield(player1, new GoblinChieftain());

        List<Permanent> chieftains = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Chieftain"))
                .toList();

        assertThat(chieftains).hasSize(2);
        for (Permanent chieftain : chieftains) {
            assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Two Goblin Chieftains give +2/+2 to other Goblins")
    void twoChieftainsStackBonuses() {
        harness.addToBattlefield(player1, new GoblinChieftain());
        harness.addToBattlefield(player1, new GoblinChieftain());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Goblin Chieftain leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new GoblinChieftain());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Goblin Chieftain"));

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Goblin Chieftain resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.setHand(player1, List.of(new GoblinChieftain()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new GoblinChieftain());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Elite Infantry"))
                .findFirst().orElseThrow();

        goblin.setPowerModifier(goblin.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(8); // 2 base + 5 spell + 1 static

        goblin.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }
}
