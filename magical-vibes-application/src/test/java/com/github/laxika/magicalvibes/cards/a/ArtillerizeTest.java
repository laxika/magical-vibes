package com.github.laxika.magicalvibes.cards.a;

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

class ArtillerizeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with artifact sacrifice puts spell on stack targeting creature")
    void castWithArtifactSacrificeTargetingCreature() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        Permanent targetCreature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(targetCreature);

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, targetCreature.getId(), artifact.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Resolving deals 5 damage to target creature")
    void resolvingDeals5DamageToCreature() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        Permanent targetCreature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(targetCreature);

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, targetCreature.getId(), artifact.getId());
        harness.passBothPriorities();

        // LlanowarElves is 1/1, so 5 damage kills it
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Resolving deals 5 damage to target player")
    void resolvingDeals5DamageToPlayer() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15); // 20 - 5
    }

    @Test
    @DisplayName("Can sacrifice a creature instead of an artifact")
    void canSacrificeCreature() {
        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot cast without an artifact or creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact non-creature permanent")
    void cannotSacrificeNonArtifactNonCreature() {
        // Use an enchantment - need to find one. Pacifism is an enchantment aura.
        Permanent enchantment = new Permanent(new com.github.laxika.magicalvibes.cards.p.Pacifism());
        gd.playerBattlefields.get(player1.getId()).add(enchantment);

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's permanent")
    void cannotSacrificeOpponentsPermanent() {
        Permanent opponentArtifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player2.getId()).add(opponentArtifact);

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Artillerize");
    }
}
