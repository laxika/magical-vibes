package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JodahArchmageEternalTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Jodah has AlternativeCostForSpellsEffect with WUBRG cost and no filter")
    void hasCorrectEffects() {
        JodahArchmageEternal card = new JodahArchmageEternal();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(AlternativeCostForSpellsEffect.class);
        AlternativeCostForSpellsEffect effect =
                (AlternativeCostForSpellsEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.manaCost()).isEqualTo("{W}{U}{B}{R}{G}");
        assertThat(effect.filter()).isNull();
    }

    // ===== Casting Jodah =====

    @Test
    @DisplayName("Casting Jodah puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new JodahArchmageEternal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Jodah, Archmage Eternal");
    }

    @Test
    @DisplayName("Resolving Jodah puts it onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new JodahArchmageEternal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Jodah, Archmage Eternal"));
    }

    // ===== Alternative WUBRG cost =====

    @Test
    @DisplayName("Creature with higher mana cost can be cast for WUBRG with Jodah on battlefield")
    void highCostCreatureCastForWubrg() {
        harness.addToBattlefield(player1, new JodahArchmageEternal());
        // Craw Wurm costs {4}{G}{G} (6 CMC) — with Jodah it can be cast for {W}{U}{B}{R}{G}
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Craw Wurm");
    }

    @Test
    @DisplayName("WUBRG payment is deducted from pool when using alternative cost")
    void wubrgPaymentDeducted() {
        harness.addToBattlefield(player1, new JodahArchmageEternal());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        // All mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Instant can also be cast for WUBRG with Jodah on battlefield")
    void instantCastForWubrg() {
        harness.addToBattlefield(player1, new JodahArchmageEternal());
        // Lightning Bolt costs {R} — paying WUBRG is valid but more expensive (player's choice)
        harness.setHand(player1, List.of(new LightningBolt()));
        // Only give WUBRG (not {R} alone), so the alternative cost is the only option
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }

    @Test
    @DisplayName("Normal mana cost is used when player can afford it even with Jodah")
    void normalCostUsedWhenAffordable() {
        harness.addToBattlefield(player1, new JodahArchmageEternal());
        // Grizzly Bears costs {1}{G} — player can afford this normally
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        // Normal cost paid: {1}{G} → 2 green spent (1 colored + 1 generic)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cannot cast with insufficient mana even with Jodah (need full WUBRG)")
    void cannotCastWithInsufficientMana() {
        harness.addToBattlefield(player1, new JodahArchmageEternal());
        harness.setHand(player1, List.of(new CrawWurm()));
        // Only 4 of 5 colors — missing green
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's spells are not affected by your Jodah")
    void opponentSpellsNotAffected() {
        harness.addToBattlefield(player1, new JodahArchmageEternal());
        // Opponent tries to cast Craw Wurm with only WUBRG — should fail since Jodah is player1's
        harness.setHand(player2, List.of(new CrawWurm()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jodah effect is removed when it leaves the battlefield")
    void effectRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new JodahArchmageEternal());
        harness.setHand(player1, List.of(new CrawWurm()));

        // Remove Jodah from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Jodah, Archmage Eternal"));

        // Now Craw Wurm cannot be cast for WUBRG
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
