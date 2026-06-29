package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolymorphTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Polymorph has correct effects")
    void hasCorrectProperties() {
        Polymorph card = new Polymorph();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DestroyTargetThenRevealUntilTypeToBattlefieldEffect.class);
    }

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
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new FountainOfYouth());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Target creature should be destroyed (in graveyard)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // The found creature should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Revealed non-creature card should be shuffled back into library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Resolving with creature on top of library puts it directly onto the battlefield")
    void creatureOnTopGoesDirectlyToBattlefield() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Set up library: creature on top
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // The creature should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

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
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new FountainOfYouth());
        gd.playerDecks.get(player1.getId()).add(new FountainOfYouth());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Target was still destroyed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // No new creature on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // All non-creature cards should be back in library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Empty library — destroy still happens but no card is put onto the battlefield")
    void emptyLibrary() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        // Empty library
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Target was destroyed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

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
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new FountainOfYouth());
        gd.playerDecks.get(player2.getId()).add(new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Opponent's creature was destroyed
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // The found creature enters the battlefield under opponent's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));

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

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Polymorph"));
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Polymorph()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Polymorph still goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Polymorph"));
        // No creature was put onto the battlefield (library wasn't searched)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        // Library was not touched
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
