package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuddenSalvation.class, GrizzlyBears.class, Plains.class, Shock.class})
class SuddenSalvationTest extends BaseCardTest {

    @Test
    void returnsUpToThreePermanentsUnderTheirOwnersControlAndDrawsPerOpponent() {
        Card ownFirst = new GrizzlyBears();
        Card ownSecond = new GrizzlyBears();
        Card opponentFirst = new GrizzlyBears();
        Card opponentSecond = new GrizzlyBears();
        moveToGraveyard(player1, ownFirst, ownSecond);
        moveToGraveyard(player2, opponentFirst, opponentSecond);

        harness.setHand(player1, List.of(new SuddenSalvation()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactly(
                ownFirst.getId(), ownSecond.getId(), opponentFirst.getId(), opponentSecond.getId());

        harness.handleMultipleCardsChosen(player1,
                List.of(ownFirst.getId(), opponentFirst.getId(), opponentSecond.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, ownFirst).isTapped()).isTrue();
        assertThat(findPermanent(player2, opponentFirst).isTapped()).isTrue();
        assertThat(findPermanent(player2, opponentSecond).isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ownSecond.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownSecond);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    void onlyCardsPutThereFromTheBattlefieldThisTurnAndPermanentCardsCanBeTargeted() {
        Card oldPermanent = new GrizzlyBears();
        Card nonPermanent = new Shock();
        Card eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldPermanent, nonPermanent));
        moveToGraveyard(player1, eligible);

        SuddenSalvation spell = new SuddenSalvation();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, eligible).isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(oldPermanent, nonPermanent, spell)
                .doesNotContain(eligible);
    }

    private void moveToGraveyard(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        for (Card card : cards) {
            Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
            harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        }
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
