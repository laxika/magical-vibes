package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunAgroundTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Run Aground has correct effects")
    void hasCorrectEffects() {
        RunAground card = new RunAground();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(PutTargetOnTopOfLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Run Aground targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Run Aground");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new Pacifism());
        UUID pacifismId = harness.getPermanentId(player2, "Pacifism");

        // Attach Pacifism to Grizzly Bears
        Permanent pacifism = gqs.findPermanentById(harness.getGameData(), pacifismId);
        pacifism.setAttachedTo(bearsId);

        harness.setHand(player1, List.of(new RunAground()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, pacifismId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.Forest());
        UUID landId = harness.getPermanentId(player1, "Forest");

        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving against creatures =====

    @Test
    @DisplayName("Resolving puts creature on top of owner's library")
    void resolvingPutsCreatureOnTopOfLibrary() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Resolving against artifacts =====

    @Test
    @DisplayName("Resolving puts artifact on top of owner's library")
    void resolvingPutsArtifactOnTopOfLibrary() {
        harness.addToBattlefield(player1, new Ornithopter());
        UUID targetId = harness.getPermanentId(player1, "Ornithopter");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ornithopter"));
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Ornithopter");
    }

    // ===== Run Aground goes to graveyard =====

    @Test
    @DisplayName("Run Aground goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Run Aground"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.setHand(player2, List.of(new RunAground()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Run Aground"));
    }
}
