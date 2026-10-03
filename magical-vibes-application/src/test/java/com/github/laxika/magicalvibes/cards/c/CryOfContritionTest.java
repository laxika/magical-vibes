package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Frazzle;
import com.github.laxika.magicalvibes.cards.p.Pyromatics;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.cards.t.TinStreetHooligan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryOfContrition.class, TinStreetHooligan.class, Pyromatics.class, Frazzle.class, Repeal.class})
class CryOfContritionTest extends BaseCardTest {

    @Test
    void targetPlayerDiscardsAndHauntedCreatureDeathTriggersAnotherDiscard() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new TinStreetHooligan()).getId();
        harness.setHand(player1, List.of(new CryOfContrition(), new TinStreetHooligan()));
        harness.setHand(player2, List.of(new TinStreetHooligan()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cry of Contrition"));

        destroyWithPyromatics(creatureId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tin Street Hooligan");
    }

    @Test
    void resolvesWithoutCreaturesAndStaysInGraveyard() {
        harness.setHand(player1, List.of(new CryOfContrition()));
        harness.setHand(player2, List.of(new TinStreetHooligan()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Cry of Contrition");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyHandDoesNotPreventHauntingOwnCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new TinStreetHooligan()).getId();
        harness.setHand(player1, List.of(new CryOfContrition()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Cry of Contrition");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cry of Contrition"));

        destroyWithPyromatics(creatureId);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cry of Contrition"));
    }

    @Test
    void canTargetSelfAndLetsTargetChooseExactlyOneCard() {
        harness.setHand(player1, List.of(new CryOfContrition(), new TinStreetHooligan(), new Pyromatics()));
        harness.setHand(player2, List.of(new TinStreetHooligan()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Tin Street Hooligan");
        harness.assertInGraveyard(player1, "Pyromatics");
        harness.assertInHand(player2, "Tin Street Hooligan");
        harness.assertInGraveyard(player1, "Cry of Contrition");
    }

    @Test
    void counteredSpellDoesNotHaunt() {
        harness.addToBattlefield(player2, new TinStreetHooligan());
        CryOfContrition cry = new CryOfContrition();
        harness.setHand(player1, List.of(cry));
        harness.setHand(player2, List.of(new Frazzle(), new TinStreetHooligan()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, cry.getId());

        harness.assertInGraveyard(player1, "Cry of Contrition");
        harness.assertInHand(player2, "Tin Street Hooligan");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void hauntTargetDyingBeforeResolutionLeavesSpellInGraveyard() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new TinStreetHooligan()).getId();
        harness.setHand(player1, List.of(new CryOfContrition()));
        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, creatureId);
        destroyWithPyromatics(creatureId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cry of Contrition");
        harness.assertInGraveyard(player2, "Tin Street Hooligan");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bouncedCreatureDoesNotTriggerDiscardOrRemainHauntedWhenItReturns() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new TinStreetHooligan()).getId();
        harness.setHand(player1, List.of(new CryOfContrition()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Repeal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, 2, creatureId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Tin Street Hooligan");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        UUID returnedCreatureId = harness.getPermanentId(player2, "Tin Street Hooligan");
        destroyWithPyromatics(returnedCreatureId);

        harness.assertInGraveyard(player2, "Tin Street Hooligan");
        harness.assertInHand(player1, "Pyromatics");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cry of Contrition"));
    }

    private void destroyWithPyromatics(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Pyromatics()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
