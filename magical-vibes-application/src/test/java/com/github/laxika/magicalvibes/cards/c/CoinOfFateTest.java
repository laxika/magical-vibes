package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoinOfFate.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class CoinOfFateTest extends BaseCardTest {

    @Test
    void enteringSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new CoinOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void opponentChoosesBottomCardAndOtherReturnsTappedAsMonarch() {
        Card bottomCard = new GrizzlyBears();
        Card returnedCard = new HillGiant();
        Permanent coin = harness.addToBattlefieldAndReturn(player1, new CoinOfFate());
        coin.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(bottomCard, returnedCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ActivatedExiledCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedExiledCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(bottomCard.getId(), returnedCard.getId());

        harness.handleMultipleCardsChosen(player2, List.of(bottomCard.getId()));

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(bottomCard);
        assertThat(gd.findExiledCard(bottomCard.getId())).isNull();
        assertThat(gd.findExiledCard(returnedCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == coin.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                p -> p.getCard() == returnedCard && p.isTapped());
    }
}
