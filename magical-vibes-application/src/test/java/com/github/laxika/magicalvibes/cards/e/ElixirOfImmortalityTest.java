package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElixirOfImmortality.class, RuneclawBear.class, GiantSpider.class, CosisTrickster.class,
        EnsoulArtifact.class})
class ElixirOfImmortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack and taps the artifact")
    void activatingPutsOnStack() {
        Permanent elixir = addReadyElixir(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(elixir.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyElixir(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent elixir = addReadyElixir(player1);
        elixir.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving gains 5 life")
    void resolvingGainsFiveLife() {
        addReadyElixir(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Resolving shuffles Elixir and graveyard into library")
    void resolvingShufflesSelfAndGraveyardIntoLibrary() {
        addReadyElixir(player1);
        Card bear1 = new RuneclawBear();
        Card bear2 = new GiantSpider();
        harness.setGraveyard(player1, List.of(bear1, bear2));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Elixir should not be on battlefield
        harness.assertNotOnBattlefield(player1, "Elixir of Immortality");
        // Graveyard should be empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        // Deck should have grown by 3 (Elixir + 2 graveyard cards)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 3);
        // Elixir should be in library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elixir of Immortality"));
    }

    @Test
    @DisplayName("Resolving with empty graveyard still shuffles Elixir into library")
    void emptyGraveyardStillShufflesSelf() {
        addReadyElixir(player1);
        harness.setGraveyard(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.WHITE, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Elixir should not be on battlefield
        harness.assertNotOnBattlefield(player1, "Elixir of Immortality");
        // Deck should have grown by 1 (just the Elixir)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Elixir should be in library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elixir of Immortality"));
    }

    @Test
    @DisplayName("If Elixir leaves battlefield before resolution, graveyard is still shuffled")
    void elixirRemovedBeforeResolutionStillShufflesGraveyard() {
        Permanent elixir = addReadyElixir(player1);
        Card bear = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Remove Elixir from battlefield before resolution (e.g. destroyed in response)
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, elixir));

        harness.passBothPriorities();

        // Graveyard cards (bear + elixir that was destroyed) should be in library now
        // The graveyard should be empty after shuffling
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        // Library should have the graveyard cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 2).contains(bear, elixir.getCard());
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        addReadyElixir(player1);
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Log contains shuffle message")
    void logContainsShuffleMessage() {
        addReadyElixir(player1);
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles") && log.contains("Elixir of Immortality"));
    }

    @Test
    @DisplayName("A stolen Elixir shuffles its owner's library and only its controller's graveyard")
    void stolenElixirShufflesBothLibraries() {
        ElixirOfImmortality card = new ElixirOfImmortality();
        card.setOwnerId(player2.getId());
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(elixir.getId(), player2.getId());
        harness.addToBattlefield(player1, new CosisTrickster());
        Card controllerGraveyardCard = new RuneclawBear();
        Card ownerGraveyardCard = new GiantSpider();
        harness.setGraveyard(player1, List.of(controllerGraveyardCard));
        harness.setGraveyard(player2, List.of(ownerGraveyardCard));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 15);
        harness.setLife(player2, 15);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        assertThat(gd.playerDecks.get(player2.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).contains(controllerGraveyardCard).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ownerGraveyardCard);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof CosisTrickster);
    }

    @Test
    @DisplayName("A bounced Elixir remains in hand while its ability gains life and shuffles the graveyard")
    void bouncedElixirIsNotShuffledFromHand() {
        Permanent elixir = addReadyElixir(player1);
        Card bear = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 15);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, elixir));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Elixir of Immortality");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1)
                .contains(bear).doesNotContain(elixir.getCard());
    }

    @Test
    @DisplayName("An Aura attached to Elixir goes to the graveyard after the shuffle")
    void attachedAuraRemainsInGraveyard() {
        Permanent elixir = addReadyElixir(player1);
        elixir.setSummoningSick(false);
        EnsoulArtifact aura = new EnsoulArtifact();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, elixir.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elixir of Immortality");
        harness.assertNotOnBattlefield(player1, "Ensoul Artifact");
        assertThat(gd.playerDecks.get(player1.getId())).contains(elixir.getCard()).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(aura);
    }

    private Permanent addReadyElixir(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ElixirOfImmortality());
    }
}
