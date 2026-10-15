package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelfireIgnition;
import com.github.laxika.magicalvibes.cards.d.DawnhartMentor;
import com.github.laxika.magicalvibes.cards.f.FestivalCrasher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SungoldSentinel.class, DawnhartMentor.class, FestivalCrasher.class, AngelfireIgnition.class})
class SungoldSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Entering exiles up to one target card from a graveyard")
    void enteringExilesTargetCardFromAnyGraveyard() {
        Card ownCard = new DawnhartMentor();
        Card opponentCard = new FestivalCrasher();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.castFromHand(player1, new SungoldSentinel(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(opponentCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(ownCard.getId());
    }

    @Test
    @DisplayName("Attacking exiles up to one target card from a graveyard")
    void attackingExilesTargetCardFromAnyGraveyard() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        sentinel.setSummoningSick(false);
        Card card = new DawnhartMentor();
        harness.setGraveyard(player2, List.of(card));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId).contains(card.getId());
    }

    @Test
    @DisplayName("Coven grants chosen-color hexproof and evasion to Sungold Sentinel")
    void covenGrantsChosenColorHexproofAndEvasion() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.addToBattlefield(player1, new DawnhartMentor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sentinel), null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasHexproofFromColor(gd, sentinel, CardColor.RED)).isTrue();

        Permanent redBlocker = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        sentinel.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(redBlocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Coven cannot be activated without three different powers")
    void covenRequiresThreeDifferentPowers() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(sentinel), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    @Test
    void enteringCanChooseZeroTargetsWithCardsAvailable() {
        Card card = new FestivalCrasher();
        harness.setGraveyard(player1, List.of(card));
        harness.castFromHand(player1, new SungoldSentinel(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void attackingCanChooseZeroTargetsWithCardsAvailable() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        sentinel.setSummoningSick(false);
        Card card = new FestivalCrasher();
        harness.setGraveyard(player1, List.of(card));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void covenStillResolvesAfterDifferentPowersAreLost() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new DawnhartMentor());
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(mentor);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.hasHexproofFromColor(gd, sentinel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, sentinel, CardColor.RED)).isFalse();
    }

    @Test
    void threeCreaturesWithOnlyTwoDifferentPowersDoNotEnableCoven() {
        harness.addToBattlefield(player1, new SungoldSentinel());
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    @Test
    void chosenColorHexproofAndEvasionExpireAtEndOfTurn() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        harness.addToBattlefield(player1, new DawnhartMentor());
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasHexproofFromColor(gd, sentinel, CardColor.RED)).isFalse();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FestivalCrasher());
        sentinel.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void hexproofRejectsOpponentMulticoloredSpellContainingChosenColor() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        harness.addToBattlefield(player1, new DawnhartMentor());
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new AngelfireIgnition()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castSorcery(player2, 0, sentinel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
