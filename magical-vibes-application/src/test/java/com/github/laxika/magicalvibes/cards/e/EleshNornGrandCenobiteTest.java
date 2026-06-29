package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
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

class EleshNornGrandCenobiteTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Elesh Norn has two static boost effects")
    void hasCorrectProperties() {
        EleshNornGrandCenobite card = new EleshNornGrandCenobite();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(StaticBoostEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(StaticBoostEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new EleshNornGrandCenobite()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Elesh Norn, Grand Cenobite");
    }

    @Test
    @DisplayName("Resolving puts Elesh Norn onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new EleshNornGrandCenobite()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"));
    }

    // ===== Static effect: does not buff itself =====

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());

        Permanent norn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, norn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, norn)).isEqualTo(7);
    }

    // ===== Static effect: buffs other own creatures =====

    @Test
    @DisplayName("Other own creatures get +2/+2")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff opponent creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Opponent bears get -2/-2 (not +2/+2): 2-2=0, 2-2=0
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(0);
    }

    // ===== Static effect: debuffs opponent creatures =====

    @Test
    @DisplayName("Opponent creatures get -2/-2")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not debuff own creatures")
    void doesNotDebuffOwnCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Own bears get +2/+2 only: 2+2=4, 2+2=4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Elesh Norns buff each other")
    void twoNornsBuffEachOther() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());

        List<Permanent> norns = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"))
                .toList();

        assertThat(norns).hasSize(2);
        for (Permanent norn : norns) {
            // Each gets +2/+2 from the other → 6/9
            assertThat(gqs.getEffectivePower(gd, norn)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, norn)).isEqualTo(9);
        }
    }

    @Test
    @DisplayName("Two Elesh Norns give +4/+4 to other own creatures")
    void twoNornsStackOwnBonus() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // 2/2 base + 4/4 from two Norns = 6/6
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Two Elesh Norns give -4/-4 to opponent creatures")
    void twoNornsStackOpponentPenalty() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // 2/2 base - 4/4 from two Norns = -2/-2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(-2);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Own creature bonus is removed when Elesh Norn leaves")
    void ownBonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent creature penalty is removed when Elesh Norn leaves")
    void opponentPenaltyRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Elesh Norn resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EleshNornGrandCenobite()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        Permanent ownBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Before casting, no bonus
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // After resolving, own creature buffed, opponent creature debuffed
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(0);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Add a temporary spell boost
        bears.setPowerModifier(bears.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7); // 2 base + 3 spell + 2 static

        // Reset end-of-turn modifiers
        bears.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4); // 2 base + 2 static
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    // ===== Vigilance keyword =====

    @Test
    @DisplayName("Elesh Norn has vigilance")
    void hasVigilance() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());

        Permanent norn = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, norn, Keyword.VIGILANCE)).isTrue();
    }
}
