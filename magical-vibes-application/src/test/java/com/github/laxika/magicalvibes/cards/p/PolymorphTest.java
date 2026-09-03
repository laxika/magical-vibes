package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Polymorph.class, DrudgeSkeletons.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class PolymorphTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a non-creature permanent with Polymorph")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID artifactId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving destroys the target creature and puts a new creature from library onto the battlefield")
    void resolvingDestroysAndPutsCreatureOnBattlefield() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library: non-creature on top, creature underneath
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Target creature should be destroyed (in graveyard)
        harness.assertInGraveyard(player1, "Llanowar Elves");

        // The found creature should be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Revealed non-creature card should be shuffled back into library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Polymorph destroys a creature despite a regeneration shield")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drudge Skeletons");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving with creature on top of library puts it directly onto the battlefield")
    void creatureOnTopGoesDirectlyToBattlefield() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library: creature on top
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // The creature should be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Library should be empty (only had the one creature)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No creature in library — all cards are shuffled back")
    void noCreatureInLibrary() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library with only non-creature cards
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new FountainOfYouth()));

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Target was still destroyed
        harness.assertInGraveyard(player1, "Llanowar Elves");

        // No new creature on battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // All non-creature cards should be back in library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty library — destroy still happens but no card is put onto the battlefield")
    void emptyLibrary() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setLibrary(player1, List.of());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Target was destroyed
        harness.assertInGraveyard(player1, "Llanowar Elves");

        // Library should still be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target opponent's creature — opponent's library is revealed")
    void targetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up opponent's library with creature
        harness.setLibrary(player2, List.of(new FountainOfYouth(), new LlanowarElves()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Opponent's creature was destroyed
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // The found creature enters the battlefield under opponent's control
        harness.assertOnBattlefield(player2, "Llanowar Elves");

        // Opponent's non-creature cards are shuffled back into their library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Polymorph goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Polymorph");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Polymorph still goes to graveyard
        harness.assertInGraveyard(player1, "Polymorph");
        // No creature was put onto the battlefield (library wasn't searched)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        // Library was not touched
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
