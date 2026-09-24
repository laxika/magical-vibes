package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnbreakReclaimer.class, GrizzlyBears.class, Island.class})
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

    private void setUpGraveyards(List<Card> ownCards, List<Card> opponentCards) {
        harness.setGraveyard(player1, ownCards);
        harness.setGraveyard(player2, opponentCards);
        harness.addToBattlefield(player1, new DawnbreakReclaimer());
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.passUntil(player1, com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
