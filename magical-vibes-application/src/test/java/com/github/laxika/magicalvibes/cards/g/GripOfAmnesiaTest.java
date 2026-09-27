package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorderPatrol.class, Forest.class, GripOfAmnesia.class, GrizzlyBears.class})
class GripOfAmnesiaTest extends BaseCardTest {

    @Test
    void exilesTargetSpellControllersGraveyardAndDrawsWithoutCountering() {
        Card firstGraveyardCard = new BorderPatrol();
        Card secondGraveyardCard = new BorderPatrol();
        Card otherPlayersGraveyardCard = new BorderPatrol();
        Card drawCard = new BorderPatrol();
        harness.setGraveyard(player1, List.of(otherPlayersGraveyardCard));
        BorderPatrol spell = castAgainstOpponent(List.of(firstGraveyardCard, secondGraveyardCard), drawCard);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(firstGraveyardCard, secondGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherPlayersGraveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell);
    }

    @Test
    void declinesToExileAndCountersTheSpellThenDraws() {
        Card graveyardCard = new BorderPatrol();
        Card drawCard = new BorderPatrol();
        BorderPatrol spell = castAgainstOpponent(List.of(graveyardCard), drawCard);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(graveyardCard, spell);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() == spell);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void mayExileAnEmptyGraveyardAndLeaveTheSpellUncountered() {
        Card drawCard = new BorderPatrol();
        BorderPatrol spell = castAgainstOpponent(List.of(), drawCard);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell);
    }

    @Test
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new BorderPatrol());
        GripOfAmnesia grip = new GripOfAmnesia();
        harness.setHand(player1, List.of(grip));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(grip);
        assertThat(gd.stack).isEmpty();
    }

    private BorderPatrol castAgainstOpponent(List<Card> graveyard, Card drawCard) {
        BorderPatrol spell = new BorderPatrol();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player2, graveyard);
        harness.setLibrary(player1, List.of(drawCard));

        harness.castFromHand(player2, spell, "{4}{W}");
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new GripOfAmnesia()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        return spell;
    }

    @Test
    void exilesEveryCardFromTargetSpellControllersGraveyard() {
        Card firstGraveyardCard = new Forest();
        Card secondGraveyardCard = new Forest();
        Card drawCard = new Forest();
        castAgainstOpponentForJudReview(List.of(firstGraveyardCard, secondGraveyardCard), drawCard);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(firstGraveyardCard, secondGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    private GrizzlyBears castAgainstOpponentForJudReview(List<Card> graveyard, Card drawCard) {
        GrizzlyBears spell = new GrizzlyBears();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player2, graveyard);
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player1, List.of(new GripOfAmnesia()));

        harness.castFromHand(player2, spell, "{1}{G}");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        return spell;
    }
}
