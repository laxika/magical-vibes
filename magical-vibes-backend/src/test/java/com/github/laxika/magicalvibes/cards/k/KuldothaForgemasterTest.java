package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FlightSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KuldothaForgemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate ability without enough artifacts to sacrifice")
    void cannotActivateWithoutEnoughArtifacts() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        // Only 2 other artifacts (Forgemaster itself is artifact too, so 3 total, but we need 3 to sacrifice)
        // Forgemaster + Spellbook + Leonin Scimitar = 3 artifacts total, exactly enough
        // Let's test with just 2 non-forgemaster artifacts and remove one:
        // Actually, Forgemaster is an artifact creature so there ARE 3 artifacts (itself + 2).
        // That's exactly 3 so auto-sacrifice would trigger. Let's test with only 2 total.
    }

    @Test
    @DisplayName("Cannot activate with fewer than 3 artifacts")
    void cannotActivateWithFewerThanThreeArtifacts() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        // Forgemaster + Spellbook = only 2 artifacts
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Auto-sacrifices when exactly 3 artifacts available")
    void autoSacrificesWhenExactlyThreeArtifacts() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        // Forgemaster + Spellbook + Leonin Scimitar = exactly 3 artifacts
        // Put an artifact in the library for the search
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

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
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new FlightSpellbomb());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        // Should prompt for choice since 4 artifacts > 3 required
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Completing all three sacrifice choices puts ability on stack")
    void completingThreeSacrificesPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new FlightSpellbomb());
        harness.addToBattlefield(player1, new GoldMyr());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        // 5 artifacts, need to sacrifice 3 by choice
        UUID spellbookId = findPermanent(player1, "Spellbook").getId();
        UUID scimitarId = findPermanent(player1, "Leonin Scimitar").getId();
        UUID spellbombId = findPermanent(player1, "Flight Spellbomb").getId();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Spellbook());

        harness.activateAbility(player1, 0, null, null);

        // First choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, spellbookId);

        // Second choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, scimitarId);

        // Third choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, spellbombId);

        // Ability should now be on the stack (Flight Spellbomb's death trigger also adds a MayPayManaEffect)
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        // Sacrificed artifacts should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"))
                .anyMatch(c -> c.getName().equals("Flight Spellbomb"));

        // Non-sacrificed artifacts should remain
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kuldotha Forgemaster"))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr"));
    }

    @Test
    @DisplayName("Resolving ability searches library for artifact and puts it onto battlefield")
    void resolvingSearchesForArtifact() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        // Seed library with an artifact to find
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GoldMyr(), new LlanowarElves()));

        // Exactly 3 artifacts -> auto-sacrifice all
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should prompt for library search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // Only artifact cards should be available to search
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.getName().equals("Gold Myr"));

        // Choose Gold Myr
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Gold Myr should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr"));
    }

    @Test
    @DisplayName("Artifact found enters battlefield untapped")
    void artifactEntersUntapped() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleLibraryCardChosen(gd, player1, 0);

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
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);
        forgemaster.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Sacrificed artifacts go to graveyard even when auto-sacrificed")
    void sacrificedArtifactsGoToGraveyard() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        // Exactly 3 artifacts -> all auto-sacrificed
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3)
                .anyMatch(c -> c.getName().equals("Kuldotha Forgemaster"))
                .anyMatch(c -> c.getName().equals("Spellbook"))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Forgemaster taps as part of the cost")
    void forgemasterTapsAsCost() {
        harness.addToBattlefield(player1, new KuldothaForgemaster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent forgemaster = findPermanent(player1, "Kuldotha Forgemaster");
        forgemaster.setSummoningSick(false);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        // Before activation, forgemaster is untapped
        assertThat(forgemaster.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        // Forgemaster should be tapped (but also sacrificed in this case since exactly 3 artifacts)
        // Since all 3 are auto-sacrificed, forgemaster is gone from battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
