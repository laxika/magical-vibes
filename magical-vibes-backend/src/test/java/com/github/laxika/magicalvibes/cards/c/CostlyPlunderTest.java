package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CostlyPlunderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with creature sacrifice puts spell on stack")
    void castWithCreatureSacrifice() {
        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Resolving draws two cards for controller")
    void resolvingDrawsTwoCards() {
        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Hand was 1 (Costly Plunder), cast it (0), then drew 2 = 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1 + 2);
    }

    @Test
    @DisplayName("Can sacrifice an artifact instead of a creature")
    void canSacrificeArtifact() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        // Drew 2 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot cast without an artifact or creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact non-creature permanent")
    void cannotSacrificeNonArtifactNonCreature() {
        Permanent enchantment = new Permanent(new com.github.laxika.magicalvibes.cards.p.Pacifism());
        gd.playerBattlefields.get(player1.getId()).add(enchantment);

        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's permanent")
    void cannotSacrificeOpponentsPermanent() {
        Permanent opponentCreature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new CostlyPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Costly Plunder");
    }
}
