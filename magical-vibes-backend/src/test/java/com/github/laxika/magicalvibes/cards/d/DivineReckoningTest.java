package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureDestroyRestEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DivineReckoningTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Divine Reckoning has correct effect and flashback")
    void hasCorrectEffectAndFlashback() {
        DivineReckoning card = new DivineReckoning();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(EachPlayerChoosesCreatureDestroyRestEffect.class);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{5}{W}{W}");
    }

    // ===== No creatures =====

    @Test
    @DisplayName("Does nothing when no creatures are on the battlefield")
    void doesNothingWhenNoCreatures() {
        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divine Reckoning"));
    }

    // ===== Single creature per player (auto-keep) =====

    @Test
    @DisplayName("Players with exactly one creature auto-keep it, no prompt")
    void singleCreatureAutoKept() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Both creatures survive — they were auto-kept since they were the only ones
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Multiple creatures — player must choose =====

    @Test
    @DisplayName("Player with multiple creatures is prompted to choose one to keep")
    void multipleCreaturesPromptsChoice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 (active player) has 2 creatures and must choose
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificeCount).isEqualTo(1);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player1.getId());
        assertThat(gd.pendingDestroyRestMode).isTrue();

        // Player1 chooses to keep Grizzly Bears
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Grizzly Bears survives, Llanowar Elves is destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Both players with multiple creatures are prompted sequentially (APNAP)")
    void bothPlayersPromptedSequentially() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 (active player, APNAP first) is prompted
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player1.getId());

        // Player1 keeps Grizzly Bears
        UUID p1BearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(p1BearsId));

        // Player2 is now prompted
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player2.getId());

        // Player2 keeps Llanowar Elves
        UUID p2ElvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.handleMultiplePermanentsChosen(player2, List.of(p2ElvesId));

        // Verify results: each player kept their chosen creature
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Mixed scenario: one player has one creature, the other has multiple =====

    @Test
    @DisplayName("Player with one creature auto-keeps, other player prompted")
    void mixedAutoKeepAndPrompt() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears()); // Only one creature

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 has 2 creatures, must choose
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player1.getId());

        // Player2's single creature was auto-protected
        assertThat(gd.pendingDestroyRestProtectedIds).hasSize(1);

        // Player1 keeps LlanowarElves
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.handleMultiplePermanentsChosen(player1, List.of(elvesId));

        // Player1: Llanowar Elves kept, Grizzly Bears destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Player2: Grizzly Bears auto-kept
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Indestructible =====

    @Test
    @DisplayName("Indestructible creature survives even when not chosen")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        // Grant Llanowar Elves indestructible
        Permanent elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();
        elves.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 chooses to keep Grizzly Bears
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Both survive — Bears was chosen, Elves is indestructible
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Regeneration =====

    @Test
    @DisplayName("Creature with regeneration shield survives when not chosen")
    void regenerationShieldSurvives() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        // Give Llanowar Elves a regeneration shield
        Permanent elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();
        elves.setRegenerationShield(1);

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 chooses to keep Grizzly Bears
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Both survive — Bears was chosen, Elves regenerated
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback from graveyard works correctly")
    void flashbackWorks() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.setGraveyard(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Player1 must choose a creature to keep
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingDestroyRestMode).isTrue();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Grizzly Bears survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Single creature auto-resolves, no prompt needed
        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Divine Reckoning"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divine Reckoning"));
    }

    // ===== Spell goes to graveyard (normal cast) =====

    @Test
    @DisplayName("Spell goes to graveyard after normal cast")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divine Reckoning"));
    }

    // ===== Only one player has creatures =====

    @Test
    @DisplayName("Works correctly when only one player has creatures")
    void onlyOnePlayerHasCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        // Player2 has no creatures

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 must choose one to keep
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }
}
