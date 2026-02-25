package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KuldothaRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices an artifact and puts spell on stack")
    void castingSacrificesArtifactAndPutsOnStack() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kuldotha Rebirth");

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Resolving creates three 1/1 red Goblin tokens")
    void resolvingCreatesThreeGoblinTokens() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> goblins = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .toList();
        assertThat(goblins).hasSize(3);

        for (Permanent goblin : goblins) {
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        }
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact permanent")
    void cannotSacrificeNonArtifact() {
        Permanent creature = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's artifact")
    void cannotSacrificeOpponentsArtifact() {
        Permanent opponentArtifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player2.getId()).add(opponentArtifact);

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kuldotha Rebirth");
    }

    @Test
    @DisplayName("Can sacrifice artifact creature as the cost")
    void canSacrificeArtifactCreature() {
        // Leonin Scimitar is an artifact equipment, use it as the sacrifice target
        Permanent artifactEquipment = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player1.getId()).add(artifactEquipment);

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifactEquipment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> goblins = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .toList();
        assertThat(goblins).hasSize(3);
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
