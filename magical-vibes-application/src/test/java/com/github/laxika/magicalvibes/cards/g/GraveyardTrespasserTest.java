package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Defenestrate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveyardTrespasser.class, GraveyardGlutton.class, Defenestrate.class})
class GraveyardTrespasserTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a creature card and applies the life changes")
    void etbExilesCreatureCard() {
        Card creature = new GraveyardTrespasser();
        harness.setGraveyard(player2, List.of(creature));
        castTrespasser();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("ETB does not apply the life changes for a noncreature card")
    void etbExilesNoncreatureCardWithoutLifeChanges() {
        Card instant = new Defenestrate();
        harness.setGraveyard(player2, List.of(instant));
        castTrespasser();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(instant);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The back face applies its life rider once per creature card")
    void backFaceScalesLifeChangesPerCreatureCard() {
        gd.dayNight = DayNight.NIGHT;
        Card ownCreature = new GraveyardTrespasser();
        Card opponentCreature = new GraveyardTrespasser();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        castTrespasser();
        harness.passBothPriorities();

        Permanent glutton = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(glutton.isTransformed()).isTrue();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCreature);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Day and night transform the card's faces")
    void dayAndNightTransformTheFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent trespasser = harness.addToBattlefieldAndReturn(player1, new GraveyardTrespasser());

        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);
        assertThat(trespasser.isTransformed()).isTrue();

        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player2);
        assertThat(trespasser.isTransformed()).isFalse();
    }

    @Test
    void etbMayChooseNoCards() {
        Card creature = new GraveyardTrespasser();
        harness.setGraveyard(player2, List.of(creature));
        castTrespasser();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void emptyGraveyardsCauseNoLifeChanges() {
        castTrespasser();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Graveyard Trespasser");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackingFrontFaceExilesFromOwnGraveyard() {
        addCreatureReady(player1, new GraveyardTrespasser());
        gd.dayNight = DayNight.DAY;
        Card creature = new GraveyardTrespasser();
        harness.setGraveyard(player1, List.of(creature));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            resolveAllTriggers();
        });

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void attackingBackFaceExilesMixedCardTypesWithOnlyOneLifeRider() {
        gd.dayNight = DayNight.NIGHT;
        castTrespasser();
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
        Card creature = new GraveyardTrespasser();
        Card instant = new Defenestrate();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(instant));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), instant.getId()));
            resolveAllTriggers();
        });

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void backFaceLifeRiderCountsOnlyTargetsStillInGraveyards() {
        gd.dayNight = DayNight.NIGHT;
        Card remaining = new GraveyardTrespasser();
        Card removed = new GraveyardTrespasser();
        harness.setGraveyard(player2, List.of(remaining, removed));
        castTrespasser();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), removed.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setExile(player2, List.of(removed));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(remaining, removed);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void frontFaceWardCountersSpellWhenOpponentCannotDiscard() {
        assertWardCountersWithoutDiscard(DayNight.DAY);
    }

    @Test
    void backFaceWardCountersSpellWhenOpponentCannotDiscard() {
        assertWardCountersWithoutDiscard(DayNight.NIGHT);
    }

    @Test
    void frontFaceWardAllowsSpellWhenOpponentDiscards() {
        assertWardAllowsDiscardPayment(DayNight.DAY);
    }

    @Test
    void backFaceWardAllowsSpellWhenOpponentDiscards() {
        assertWardAllowsDiscardPayment(DayNight.NIGHT);
    }

    @Test
    void controllersOwnSpellDoesNotTriggerWard() {
        Permanent trespasser = harness.addToBattlefieldAndReturn(player1, new GraveyardTrespasser());
        harness.setHand(player1, List.of(new Defenestrate()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, trespasser.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Graveyard Trespasser");
        harness.assertInGraveyard(player1, "Graveyard Trespasser");
    }

    @Test
    void dayboundDoesNotCreateAnAdditionalUpkeepTrigger() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new GraveyardTrespasser());
        gd.spellsCastLastTurn.clear();
        harness.forceStep(TurnStep.UPKEEP);
        gd.additionalUpkeepsRemaining = 1;

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nightboundDoesNotTriggerForSpellsCastByNonactivePlayer() {
        gd.dayNight = DayNight.NIGHT;
        castTrespasser();
        resolveAllTriggers();
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player2);
        harness.forceStep(TurnStep.UNTAP);

        gs.advanceStep(gd);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
    }

    private void assertWardCountersWithoutDiscard(DayNight designation) {
        gd.dayNight = designation;
        castTrespasser();
        resolveAllTriggers();
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new Defenestrate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, permanent.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(permanent);
        harness.assertInGraveyard(player2, "Defenestrate");
    }

    private void assertWardAllowsDiscardPayment(DayNight designation) {
        gd.dayNight = designation;
        castTrespasser();
        resolveAllTriggers();
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card discarded = new GraveyardTrespasser();
        harness.setHand(player2, List.of(new Defenestrate(), discarded));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(permanent);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        harness.assertInGraveyard(player1, "Graveyard Trespasser");
    }

    @Test
    void attackingMayExileNothing() {
        gd.dayNight = DayNight.DAY;
        addCreatureReady(player1, new GraveyardTrespasser());
        Card creature = new GraveyardTrespasser();
        harness.setGraveyard(player2, List.of(creature));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of());
            resolveAllTriggers();
        });

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void invalidatedFrontFaceTargetCausesNoLifeChanges() {
        Card creature = new GraveyardTrespasser();
        harness.setGraveyard(player2, List.of(creature));
        castTrespasser();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(creature));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castTrespasser() {
        harness.setHand(player1, List.of(new GraveyardTrespasser()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

}
