package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.FlightSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KuldothaForgemaster.class, FlightSpellbomb.class, GoldMyr.class,
        LeoninScimitar.class, LlanowarElves.class, Spellbook.class})
class KuldothaForgemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate with fewer than 3 artifacts")
    void cannotActivateWithFewerThanThreeArtifacts() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());

        // Forgemaster + Spellbook = only 2 artifacts
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Auto-sacrifices when exactly 3 artifacts available")
    void autoSacrificesWhenExactlyThreeArtifacts() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        // Forgemaster + Spellbook + Leonin Scimitar = exactly 3 artifacts
        // Put an artifact in the library for the search
        harness.setLibrary(player1, List.of(new GoldMyr()));

        harness.activateAbility(player1, 0, null, null);

        // All 3 artifacts should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Prompts for artifact choice when more than 3 available")
    void promptsForChoiceWithMoreThanThreeArtifacts() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new FlightSpellbomb());

        harness.activateAbility(player1, 0, null, null);

        // Should prompt for choice since 4 artifacts > 3 required
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Completing all three sacrifice choices puts ability on stack")
    void completingThreeSacrificesPutsAbilityOnStack() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new GoldMyr());

        // 5 artifacts, need to sacrifice 3 by choice
        UUID spellbookId = findPermanent(player1, "Spellbook").getId();
        UUID scimitarId = findPermanent(player1, "Leonin Scimitar").getId();
        UUID spellbombId = findPermanent(player1, "Flight Spellbomb").getId();

        harness.setLibrary(player1, List.of(new Spellbook()));

        harness.activateAbility(player1, 0, null, null);

        // First choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, spellbookId);

        // Second choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, scimitarId);

        // Third choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, spellbombId);

        // Ability should now be on the stack (Flight Spellbomb's death trigger also adds a MayPayManaEffect)
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        // Sacrificed artifacts should be in graveyard
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Flight Spellbomb");

        // Non-sacrificed artifacts should remain
        harness.assertOnBattlefield(player1, "Kuldotha Forgemaster");
        harness.assertOnBattlefield(player1, "Gold Myr");
    }

    @Test
    @DisplayName("Resolving ability searches library for artifact and puts it onto battlefield")
    void resolvingSearchesForArtifact() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        // Seed library with an artifact to find
        harness.setLibrary(player1, List.of(new GoldMyr(), new LlanowarElves()));

        // Exactly 3 artifacts -> auto-sacrifice all
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should prompt for library search
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Only artifact cards should be available to search
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Gold Myr"));

        // Choose Gold Myr
        harness.handleCardChosen(player1, 0);

        // Gold Myr should be on the battlefield
        harness.assertOnBattlefield(player1, "Gold Myr");
    }

    @Test
    @DisplayName("Artifact found enters battlefield untapped")
    void artifactEntersUntapped() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.setLibrary(player1, List.of(new GoldMyr()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent goldMyr = findPermanent(player1, "Gold Myr");
        assertThat(goldMyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate when summoning sick")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent forgemaster = addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        forgemaster.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Sacrificed artifacts go to graveyard even when auto-sacrificed")
    void sacrificedArtifactsGoToGraveyard() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.setLibrary(player1, List.of(new GoldMyr()));

        // Exactly 3 artifacts -> all auto-sacrificed
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3)
                .anyMatch(c -> c.getName().equals("Kuldotha Forgemaster"))
                .anyMatch(c -> c.getName().equals("Spellbook"))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Forgemaster taps when three other artifacts are sacrificed")
    void forgemasterTapsAsCost() {
        Permanent forgemaster = addCreatureReady(player1, new KuldothaForgemaster());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GoldMyr());
        }
        List<UUID> sacrificeIds = findPermanents(player1, "Gold Myr").stream()
                .map(Permanent::getId).toList();

        harness.activateAbility(player1, 0, null, null);
        for (UUID id : sacrificeIds) {
            harness.handlePermanentChosen(player1, id);
        }

        assertThat(forgemaster.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Kuldotha Forgemaster");
        harness.assertNotOnBattlefield(player1, "Gold Myr");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent artifacts and nonartifact creatures cannot pay the sacrifice cost")
    void cannotUseOpponentArtifactsOrNonartifacts() {
        Permanent forgemaster = addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GoldMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(forgemaster.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Gold Myr");
    }

    @Test
    @DisplayName("May fail to find even when an artifact is available")
    void mayFailToFindAvailableArtifact() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        GoldMyr artifact = new GoldMyr();
        harness.setLibrary(player1, List.of(artifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Ability resolves with an empty library after sacrificing its source")
    void resolvesWithEmptyLibrary() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Kuldotha Forgemaster");
    }
    @Test
    @DisplayName("Search can put a noncreature artifact onto the battlefield")
    void searchesForNoncreatureArtifact() {
        addCreatureReady(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        Spellbook artifact = new Spellbook();
        harness.setLibrary(player1, List.of(artifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.stack).isEmpty();
    }

}
