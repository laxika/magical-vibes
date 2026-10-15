package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathmistRaptor.class})
class DeathmistRaptorTest extends BaseCardTest {

    @Test
    void turnsFaceUpWithMegamorphCounter() {
        Permanent raptor = castRaptorFaceDown();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raptor));
        harness.passBothPriorities();

        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(raptor.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void mayReturnFromGraveyardFaceUpWhenPermanentTurnsFaceUp() {
        DeathmistRaptor raptorCard = prepareGraveyardTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Face up");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(raptorCard.getId())
                        && !permanent.isFaceDown());
        harness.assertNotInGraveyard(player1, "Deathmist Raptor");
    }

    @Test
    void mayReturnFromGraveyardFaceDownWhenPermanentTurnsFaceUp() {
        DeathmistRaptor raptorCard = prepareGraveyardTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Face down");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(raptorCard.getId())
                        && permanent.isFaceDown());
        harness.assertNotInGraveyard(player1, "Deathmist Raptor");
    }

    @Test
    void mayDeclineReturningFromGraveyard() {
        DeathmistRaptor raptorCard = prepareGraveyardTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Deathmist Raptor");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(raptorCard.getId()));
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphDoesNotAddCounter() {
        Permanent raptor = castRaptorFaceDown();

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, raptor);
        harness.passBothPriorities();

        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(raptor.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void opponentTurningPermanentFaceUpDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DeathmistRaptor()));
        Permanent opponentRaptor = harness.addToBattlefieldAndReturn(player2, new DeathmistRaptor());
        opponentRaptor.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, opponentRaptor);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Deathmist Raptor");
        harness.assertNotOnBattlefield(player1, "Deathmist Raptor");
    }

    @Test
    void returnedFaceDownRaptorCanTurnFaceUpForMegamorphCost() {
        DeathmistRaptor raptorCard = prepareGraveyardTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Face down");
        harness.passBothPriorities();
        Permanent returnedRaptor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(raptorCard.getId()))
                .findFirst().orElseThrow();

        assertThat(returnedRaptor.isFaceDown()).isTrue();
        assertThat(returnedRaptor.getPlusOnePlusOneCounters()).isZero();
        assertThat(returnedRaptor.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returnedRaptor));

        assertThat(returnedRaptor.isFaceDown()).isFalse();
        assertThat(returnedRaptor.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    private DeathmistRaptor prepareGraveyardTrigger() {
        DeathmistRaptor raptorCard = new DeathmistRaptor();
        harness.setGraveyard(player1, List.of(raptorCard));
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player1, new DeathmistRaptor());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, faceDownCreature);
        harness.passBothPriorities();
        return raptorCard;
    }

    private Permanent castRaptorFaceDown() {
        harness.setHand(player1, List.of(new DeathmistRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Deathmist Raptor");
    }
}
