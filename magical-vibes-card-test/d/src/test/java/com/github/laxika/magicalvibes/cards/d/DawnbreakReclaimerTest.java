package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnbreakReclaimer.class, GrizzlyBears.class, Island.class, SoulWarden.class})
class DawnbreakReclaimerTest extends BaseCardTest {

    @Test
    void controllerAndOpponentChooseCardsThenMayReturnThemUnderTheirOwnersControl() {
        Card opponentUnchosen = new GrizzlyBears();
        Card opponentChosen = new GrizzlyBears();
        Card ownChosen = new GrizzlyBears();
        Card ownUnchosen = new GrizzlyBears();
        setUpGraveyards(List.of(ownChosen, ownUnchosen), List.of(opponentUnchosen, opponentChosen));

        resolveEndStepTrigger();

        PendingInteraction.GraveyardChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.cardPool()).containsExactly(opponentUnchosen, opponentChosen);
        harness.handleGraveyardCardChosen(player1, opponentChoice.cardPool().indexOf(opponentChosen));

        PendingInteraction.GraveyardChoice ownChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(ownChoice.playerId()).isEqualTo(player2.getId());
        assertThat(ownChoice.cardPool()).containsExactly(ownChosen, ownUnchosen);
        harness.handleGraveyardCardChosen(player2, ownChoice.cardPool().indexOf(ownChosen));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownChosen.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opponentChosen.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(ownUnchosen.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opponentUnchosen.getId());
    }

    @Test
    void decliningReturnLeavesBothChosenCardsInTheirGraveyards() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        setUpGraveyards(List.of(ownCard), List.of(opponentCard));

        resolveEndStepTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(ownCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opponentCard.getId());
    }

    @Test
    void opponentChoosesFromOwnGraveyardWhenNoOpponentCreatureIsAvailable() {
        Card ownUnchosen = new GrizzlyBears();
        Card ownChosen = new GrizzlyBears();
        Card nonCreature = new Island();
        setUpGraveyards(List.of(ownUnchosen, ownChosen), List.of(nonCreature));

        resolveEndStepTrigger();

        PendingInteraction.GraveyardChoice ownChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(ownChoice.playerId()).isEqualTo(player2.getId());
        assertThat(ownChoice.cardPool()).containsExactly(ownUnchosen, ownChosen);
        harness.handleGraveyardCardChosen(player2, ownChoice.cardPool().indexOf(ownChosen));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownChosen.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(ownUnchosen.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(nonCreature.getId());
    }

    @Test
    void acceptingReturnFinishesTheOriginalAbilityWithoutAnotherPriorityRound() {
        Card ownCard = new DawnbreakReclaimer();
        Card opponentCard = new DawnbreakReclaimer();
        setUpGraveyards(List.of(ownCard), List.of(opponentCard));

        resolveEndStepTrigger();

        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opponentCard.getId()));
    }

    @Test
    void canReturnOnlyTheOpponentCreatureWhenOwnGraveyardHasNoCreature() {
        Card nonCreature = new Island();
        Card opponentCard = new DawnbreakReclaimer();
        setUpGraveyards(List.of(nonCreature), List.of(opponentCard));

        resolveEndStepTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opponentCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void noCreatureCardsInEitherGraveyardFinishesWithoutAChoice() {
        Card ownLand = new Island();
        Card opponentLand = new Island();
        setUpGraveyards(List.of(ownLand), List.of(opponentLand));

        resolveEndStepTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Card ownCard = new DawnbreakReclaimer();
        Card opponentCard = new DawnbreakReclaimer();
        setUpGraveyards(List.of(ownCard), List.of(opponentCard));

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void returningCreaturesSimultaneouslyLetsEachSeeTheOtherEnter() {
        Card ownCard = new SoulWarden();
        Card opponentCard = new SoulWarden();
        setUpGraveyards(List.of(ownCard), List.of(opponentCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStepTrigger();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleMayAbilityChosen(player1, true);
            for (int priorityRound = 0; priorityRound < 3 && !gd.stack.isEmpty(); priorityRound++) {
                harness.passBothPriorities();
            }
        });

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void setUpGraveyards(List<Card> ownCards, List<Card> opponentCards) {
        harness.setGraveyard(player1, ownCards);
        harness.setGraveyard(player2, opponentCards);
        harness.addToBattlefield(player1, new DawnbreakReclaimer());
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
