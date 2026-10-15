package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallToTheKindred.class, GrizzlyBears.class, LlanowarElves.class, Shock.class,
        AirElemental.class, Plains.class, FountainOfYouth.class, Naturalize.class, AmoeboidChangeling.class})
class CallToTheKindredTest extends BaseCardTest {

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
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        harness.setHand(player1, List.of(new CallToTheKindred()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

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

    @Test
    @DisplayName("Upkeep prompts controller with may ability to look")
    void upkeepPromptsMayAbility() {
        setupAuraOnBears();
        setupLibraryTopFive(List.of(
                new GrizzlyBears(), new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining to look does nothing")
    void decliningDoesNothing() {
        setupAuraOnBears();
        GrizzlyBears topBear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                topBear, new LlanowarElves(), new Shock(), new Plains(), new Plains()
        ));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, false); // decline

        // Library is unchanged — top card is still the bear
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topBear);
    }

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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        // Only the Bear should be offered
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Grizzly Bears");
    }

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
        harness.handleCardChosen(player1, 0);

        // Bear should be on the battlefield
        long bearsOnBattlefield = countPermanents(player1, "Grizzly Bears");
        assertThat(bearsOnBattlefield).isEqualTo(2); // original enchanted + new one

        // Remaining 4 cards should be in reorder phase
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

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
        harness.handleCardChosen(player1, -1);

        // No new permanent on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        // All 5 cards should be reordered to bottom
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsOnly("Grizzly Bears");
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        setupAuraOnBears();
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Removing the Aura in response does not stop its upkeep ability")
    void destroyedAuraUsesLastKnownAttachment() {
        setupAuraOnBears();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear, new Plains()));
        harness.setHand(player1, List.of(new Naturalize()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Call to the Kindred").getId());
        harness.assertNotOnBattlefield(player1, "Call to the Kindred");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(bear);
        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the enchanted creature in response uses its last known creature types")
    void destroyedCreatureUsesLastKnownTypes() {
        setupAuraOnBears();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear, new Plains()));
        harness.setHand(player1, List.of(new Shock()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(bear);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that loses all creature types cannot match a creature card")
    void losingCreatureTypesPreventsMatching() {
        setupAuraOnBears();
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Plains()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 2, 1, null, findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("A creature granted all creature types can match a different printed creature type")
    void gainingAllCreatureTypesAllowsDifferentCreature() {
        setupAuraOnBears();
        addCreatureReady(player1, new AmoeboidChangeling());
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(elves, new Plains()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 2, 0, null, findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(elves);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Only the top five cards are examined and the remainder goes below untouched cards in chosen order")
    void remainingCardsGoToBottomInChosenOrder() {
        setupAuraOnBears();
        GrizzlyBears bear = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains1 = new Plains();
        Plains plains2 = new Plains();
        GrizzlyBears untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear, elves, shock, plains1, plains2, untouched));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(bear);
        harness.handleCardChosen(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, plains2, plains1, shock, elves);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library shorter than five cards is processed without drawing or losing cards")
    void shortLibraryProcessesAvailableCards() {
        setupAuraOnBears();
        GrizzlyBears bear = new GrizzlyBears();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(bear, plains));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    /**
     * Sets up Call to the Kindred attached to a Grizzly Bears on player1's battlefield.
     */
    private void setupAuraOnBears() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CallToTheKindred());
        auraPerm.setAttachedTo(creature.getId());
    }

    private void setupLibraryTopFive(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
