package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallToTheKindredTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Call to the Kindred has correct effect structure")
    void hasCorrectEffects() {
        CallToTheKindred card = new CallToTheKindred();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(may.wrapped()).isInstanceOf(LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect.class);
        LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect effect =
                (LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect) may.wrapped();
        assertThat(effect.count()).isEqualTo(5);
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target a creature with Call to the Kindred")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CallToTheKindred()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        harness.setHand(player1, List.of(new CallToTheKindred()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Resolving aura =====

    @Test
    @DisplayName("Resolving Call to the Kindred attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CallToTheKindred()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Call to the Kindred")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== Upkeep trigger — may prompt =====

    @Test
    @DisplayName("Upkeep prompts controller with may ability to look")
    void upkeepPromptsMayAbility() {
        setupAuraOnBears();
        setupLibraryTopFive(List.of(
                new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    // ===== Declining to look =====

    @Test
    @DisplayName("Declining to look does nothing")
    void decliningDoesNothing() {
        setupAuraOnBears();
        GrizzlyBears topBear = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                topBear, new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, false); // decline

        // Library is unchanged — top card is still the bear
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topBear);
    }

    // ===== Accepting — with matching creature =====

    @Test
    @DisplayName("Accepting look offers creature cards sharing a type with enchanted creature")
    void acceptingOffersSharingCreatureType() {
        setupAuraOnBears(); // enchanted creature is Bear

        GrizzlyBears bear2 = new GrizzlyBears(); // Bear — should match
        LlanowarElves elves = new LlanowarElves(); // Elf Druid — should NOT match
        setupLibraryTopFive(List.of(
                bear2, elves, new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // accept look

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
        // Only the Bear should be offered
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Choosing a creature puts it onto the battlefield =====

    @Test
    @DisplayName("Choosing a matching creature puts it onto the battlefield")
    void choosingPutsOnBattlefield() {
        setupAuraOnBears();

        GrizzlyBears bear2 = new GrizzlyBears();
        setupLibraryTopFive(List.of(
                bear2, new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Choose the Bear
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Bear should be on the battlefield
        long bearsOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(bearsOnBattlefield).isEqualTo(2); // original enchanted + new one

        // Remaining 4 cards should be in reorder phase
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(4);
    }

    // ===== May decline to put creature =====

    @Test
    @DisplayName("May decline to put a creature onto the battlefield")
    void mayDeclineToPutCreature() {
        setupAuraOnBears();

        setupLibraryTopFive(List.of(
                new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Decline to choose (index -1)
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // No new permanent on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        // All 5 cards should be reordered to bottom
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    // ===== No matching creatures =====

    @Test
    @DisplayName("No matching creatures means all cards go to bottom")
    void noMatchingCreaturesReordersAll() {
        setupAuraOnBears(); // enchanted creature is Bear

        // No Bears in top 5
        setupLibraryTopFive(List.of(
                new LlanowarElves(), new AirElemental(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // No matching creatures — directly to reorder
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    // ===== Multiple matching creatures =====

    @Test
    @DisplayName("Multiple matching creatures are all offered for selection")
    void multipleMatchingCreaturesAllOffered() {
        setupAuraOnBears();

        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        setupLibraryTopFive(List.of(
                bear1, bear2, new LlanowarElves(), new Shock(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsOnly("Grizzly Bears");
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        setupAuraOnBears();
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Trigger fires only during controller's upkeep =====

    @Test
    @DisplayName("Trigger does NOT fire during opponent's upkeep")
    void triggerDoesNotFireDuringOpponentUpkeep() {
        setupAuraOnBears();
        setupLibraryTopFive(List.of(
                new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        // No may prompt should appear
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Helpers =====

    /**
     * Sets up Call to the Kindred attached to a Grizzly Bears on player1's battlefield.
     */
    private void setupAuraOnBears() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent auraPerm = new Permanent(new CallToTheKindred());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
    }

    private void setupLibraryTopFive(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
