package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCardIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HoardingDragonTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB may-search-artifact and death may-return-to-hand effects")
    void hasCorrectEffects() {
        HoardingDragon card = new HoardingDragon();

        // ETB: MayEffect wrapping SearchLibraryForCardTypeToExileAndImprintEffect(ARTIFACT)
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect etbMay = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etbMay.wrapped()).isInstanceOf(SearchLibraryForCardTypeToExileAndImprintEffect.class);
        SearchLibraryForCardTypeToExileAndImprintEffect etb =
                (SearchLibraryForCardTypeToExileAndImprintEffect) etbMay.wrapped();
        assertThat(etb.cardTypes()).containsExactly(CardType.ARTIFACT);

        // Death: MayEffect wrapping PutImprintedCardIntoOwnersHandEffect
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect deathMay = (MayEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(deathMay.wrapped()).isInstanceOf(PutImprintedCardIntoOwnersHandEffect.class);
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("ETB presents may prompt for library search")
    void etbPresentsMayPrompt() {
        setupDeck(List.of(new Spellbook(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting ETB may prompt presents only artifact cards from library")
    void acceptingEtbMayPresentsOnlyArtifacts() {
        Spellbook spellbook = new Spellbook();
        GrizzlyBears bears = new GrizzlyBears();
        setupDeck(List.of(spellbook, bears));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        // Only artifact cards should be presented (Spellbook, not Grizzly Bears)
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Choosing an artifact exiles it and imprints on Hoarding Dragon")
    void choosingArtifactExilesAndImprints() {
        Spellbook spellbook = new Spellbook();
        setupDeck(List.of(spellbook, new GrizzlyBears()));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Choose the artifact (Spellbook)
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Spellbook should be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));

        // Hoarding Dragon should have Spellbook imprinted
        Permanent dragon = findPermanent(player1, "Hoarding Dragon");
        assertThat(dragon.getCard().getImprintedCard()).isNotNull();
        assertThat(dragon.getCard().getImprintedCard().getName()).isEqualTo("Spellbook");
    }

    @Test
    @DisplayName("Declining ETB may prompt skips the library search")
    void decliningEtbMaySkipsSearch() {
        setupDeck(List.of(new Spellbook()));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Hoarding Dragon");
        assertThat(gd.playerExiledCards.get(player1.getId())).isEmpty();
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("Death trigger returns imprinted card to owner's hand")
    void deathTriggerReturnsImprintedCardToHand() {
        HoardingDragon dragonCard = new HoardingDragon();
        harness.addToBattlefield(player1, dragonCard);

        // Manually imprint an artifact
        Spellbook spellbook = new Spellbook();
        Permanent dragon = findPermanent(player1, "Hoarding Dragon");
        dragon.getCard().setImprintedCard(spellbook);
        gd.playerExiledCards.get(player1.getId()).add(spellbook);

        // Kill Hoarding Dragon with Doom Blade
        UUID dragonId = harness.getPermanentId(player1, "Hoarding Dragon");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, dragonId);
        harness.passBothPriorities(); // resolve Doom Blade — Dragon dies
        harness.passBothPriorities(); // resolve MayEffect from stack

        // Death trigger should present may prompt
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Spellbook should be in player1's hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));

        // Spellbook should no longer be in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spellbook"));

        // Dragon should be in graveyard
        harness.assertInGraveyard(player1, "Hoarding Dragon");
    }

    @Test
    @DisplayName("Declining death trigger may prompt leaves the card in exile")
    void decliningDeathTriggerLeavesCardInExile() {
        HoardingDragon dragonCard = new HoardingDragon();
        harness.addToBattlefield(player1, dragonCard);

        // Manually imprint an artifact
        Spellbook spellbook = new Spellbook();
        Permanent dragon = findPermanent(player1, "Hoarding Dragon");
        dragon.getCard().setImprintedCard(spellbook);
        gd.playerExiledCards.get(player1.getId()).add(spellbook);

        // Kill Hoarding Dragon with Doom Blade
        UUID dragonId = harness.getPermanentId(player1, "Hoarding Dragon");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, dragonId);
        harness.passBothPriorities(); // resolve Doom Blade — Dragon dies
        harness.passBothPriorities(); // resolve MayEffect from stack

        harness.handleMayAbilityChosen(player1, false);

        // Spellbook should remain in exile
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));

        // Spellbook should NOT be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Death trigger does nothing if no card was imprinted")
    void deathTriggerDoesNothingWithNoImprint() {
        HoardingDragon dragonCard = new HoardingDragon();
        harness.addToBattlefield(player1, dragonCard);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Kill Hoarding Dragon with Doom Blade
        UUID dragonId = harness.getPermanentId(player1, "Hoarding Dragon");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, dragonId);
        harness.passBothPriorities(); // resolve Doom Blade — Dragon dies
        harness.passBothPriorities(); // resolve MayEffect from stack

        // May prompt should still fire
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Hand should be unchanged
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.assertInGraveyard(player1, "Hoarding Dragon");
    }

    // ===== Helpers =====

    private void setupDeck(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
