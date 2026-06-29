package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShapeAnewTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Shape Anew has correct card properties")
    void hasCorrectProperties() {
        ShapeAnew card = new ShapeAnew();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(SacrificeTargetThenRevealUntilTypeToBattlefieldEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Shape Anew puts it on the stack with target artifact")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Shape Anew");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a creature with Shape Anew")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving sacrifices the target artifact and puts a new artifact from library onto the battlefield")
    void resolvingSacrificesAndPutsArtifactOnBattlefield() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library: non-artifact on top, artifact underneath
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new MoxOpal());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Target artifact should be sacrificed (in graveyard)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));

        // The found artifact should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mox Opal"));

        // Revealed non-artifact card should be shuffled back into library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Resolving with artifact on top of library puts it directly onto the battlefield")
    void artifactOnTopGoesDirectlyToBattlefield() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library: artifact on top
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new MoxOpal());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // The artifact should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mox Opal"));

        // Library should be empty (only had the one artifact)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No artifact in library — all cards are shuffled back")
    void noArtifactInLibrary() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library with only non-artifact cards
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Target was still sacrificed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));

        // No new artifact on battlefield (the target was sacrificed)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mox Opal"));

        // All non-artifact cards should be back in library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty library — sacrifice still happens but no card is put onto the battlefield")
    void emptyLibrary() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Empty library
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Target was sacrificed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));

        // Library should still be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target opponent's artifact — opponent sacrifices and reveals from their library")
    void targetOpponentArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up opponent's library with artifact
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new MoxOpal());

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Opponent's artifact was sacrificed
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));

        // The found artifact enters the battlefield under opponent's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mox Opal"));

        // Opponent's non-artifact cards are shuffled back into their library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Shape Anew goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new MoxOpal());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shape Anew"));
    }

    @Test
    @DisplayName("Fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new MoxOpal());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Shape Anew still goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shape Anew"));
        // No artifact was put onto the battlefield (library wasn't searched)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        // Library was not touched
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mox Opal"));
    }
}
