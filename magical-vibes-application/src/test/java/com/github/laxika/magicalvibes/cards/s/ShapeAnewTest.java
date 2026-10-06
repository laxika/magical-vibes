package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.PrecursorGolem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShapeAnew.class, Memnite.class, CarapaceForger.class, MoxOpal.class, PrecursorGolem.class})
class ShapeAnewTest extends BaseCardTest {


    @Test
    @DisplayName("Casting Shape Anew puts it on the stack with target artifact")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID targetId = harness.getPermanentId(player1, "Memnite");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Shape Anew");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature with Shape Anew")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID creatureId = harness.getPermanentId(player2, "Carapace Forger");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Resolving sacrifices the target artifact and puts a new artifact from library onto the battlefield")
    void resolvingSacrificesAndPutsArtifactOnBattlefield() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library: non-artifact on top, artifact underneath
        harness.setLibrary(player1, List.of(new CarapaceForger(), new MoxOpal()));

        UUID targetId = harness.getPermanentId(player1, "Memnite");
        harness.castAndResolveSorcery(player1, 0, targetId);


        // Target artifact should be sacrificed (in graveyard)
        harness.assertInGraveyard(player1, "Memnite");

        // The found artifact should be on the battlefield
        harness.assertOnBattlefield(player1, "Mox Opal");

        // Revealed non-artifact card should be shuffled back into library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Carapace Forger"));
    }

    @Test
    @DisplayName("Resolving with artifact on top of library puts it directly onto the battlefield")
    void artifactOnTopGoesDirectlyToBattlefield() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library: artifact on top
        harness.setLibrary(player1, List.of(new MoxOpal()));

        UUID targetId = harness.getPermanentId(player1, "Memnite");
        harness.castAndResolveSorcery(player1, 0, targetId);


        // The artifact should be on the battlefield
        harness.assertOnBattlefield(player1, "Mox Opal");

        // Library should be empty (only had the one artifact)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No artifact in library — all cards are shuffled back")
    void noArtifactInLibrary() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library with only non-artifact cards
        harness.setLibrary(player1, List.of(new CarapaceForger(), new CarapaceForger()));

        UUID targetId = harness.getPermanentId(player1, "Memnite");
        harness.castAndResolveSorcery(player1, 0, targetId);


        // Target was still sacrificed
        harness.assertInGraveyard(player1, "Memnite");

        // No new artifact on battlefield (the target was sacrificed)
        harness.assertNotOnBattlefield(player1, "Mox Opal");

        // All non-artifact cards should be back in library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty library — sacrifice still happens but no card is put onto the battlefield")
    void emptyLibrary() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Empty library
        harness.setLibrary(player1, List.of());

        UUID targetId = harness.getPermanentId(player1, "Memnite");
        harness.castAndResolveSorcery(player1, 0, targetId);


        // Target was sacrificed
        harness.assertInGraveyard(player1, "Memnite");

        // Library should still be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target opponent's artifact — opponent sacrifices and reveals from their library")
    void targetOpponentArtifact() {
        harness.addToBattlefield(player2, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up opponent's library with artifact
        harness.setLibrary(player2, List.of(new CarapaceForger(), new MoxOpal()));

        UUID targetId = harness.getPermanentId(player2, "Memnite");
        harness.castAndResolveSorcery(player1, 0, targetId);


        // Opponent's artifact was sacrificed
        harness.assertInGraveyard(player2, "Memnite");

        // The found artifact enters the battlefield under opponent's control
        harness.assertOnBattlefield(player2, "Mox Opal");

        // Opponent's non-artifact cards are shuffled back into their library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Carapace Forger"));
    }

    @Test
    @DisplayName("Shape Anew goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setLibrary(player1, List.of(new MoxOpal()));

        UUID targetId = harness.getPermanentId(player1, "Memnite");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shape Anew");
    }

    @Test
    @DisplayName("Fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setLibrary(player1, List.of(new MoxOpal()));

        UUID targetId = harness.getPermanentId(player1, "Memnite");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Shape Anew still goes to graveyard
        harness.assertInGraveyard(player1, "Shape Anew");
        // No artifact was put onto the battlefield (library wasn't searched)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        // Library was not touched
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mox Opal"));
    }
    @Test
    @DisplayName("Stops at the first artifact, including an artifact creature")
    void stopsAtFirstArtifactCreature() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, List.of(new CarapaceForger(), new Memnite(), new MoxOpal()));

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Mox Opal"));

        harness.assertInGraveyard(player1, "Mox Opal");
        harness.assertOnBattlefield(player1, "Memnite");
        harness.assertNotOnBattlefield(player1, "Mox Opal");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Carapace Forger", "Mox Opal");
    }

    @Test
    @DisplayName("The revealed artifact creature's enters ability triggers")
    void revealedArtifactCreatureTriggersEntersAbility() {
        harness.addToBattlefield(player1, new Memnite());
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, List.of(new PrecursorGolem()));

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Memnite"));
        harness.assertOnBattlefield(player1, "Precursor Golem");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Golem"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Memnite");
    }

    @Test
    @DisplayName("Uses the controller's library while sacrificing into the owner's graveyard")
    void stolenArtifactUsesControllersLibrary() {
        Memnite stolen = new Memnite();
        stolen.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, stolen);
        harness.setHand(player1, List.of(new ShapeAnew()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, List.of(new CarapaceForger()));
        harness.setLibrary(player2, List.of(new MoxOpal()));

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Memnite"));

        harness.assertInGraveyard(player1, "Memnite");
        harness.assertNotInGraveyard(player2, "Memnite");
        harness.assertOnBattlefield(player2, "Mox Opal");
        harness.assertNotOnBattlefield(player1, "Mox Opal");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName())
                .containsExactly("Carapace Forger");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
