package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

class BenalishMarshalTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Benalish Marshal has correct card properties")
    void hasCorrectProperties() {
        BenalishMarshal card = new BenalishMarshal();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BenalishMarshal()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Benalish Marshal");
    }

    @Test
    @DisplayName("Resolving puts Benalish Marshal onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BenalishMarshal()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Benalish Marshal"));
    }

    // ===== Static effect: buffs other own creatures =====

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void buffsOtherOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BenalishMarshal());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new BenalishMarshal());

        Permanent marshal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Benalish Marshal"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Benalish Marshals buff each other")
    void twoMarshalsBuffEachOther() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player1, new BenalishMarshal());

        List<Permanent> marshals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Benalish Marshal"))
                .toList();

        assertThat(marshals).hasSize(2);
        for (Permanent marshal : marshals) {
            assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Two Benalish Marshals give +2/+2 to other creatures")
    void twoMarshalsStackBonuses() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Benalish Marshal leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Benalish Marshal"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Benalish Marshal resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BenalishMarshal()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        bears.setPowerModifier(bears.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6); // 2 base + 3 spell + 1 static

        bears.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
