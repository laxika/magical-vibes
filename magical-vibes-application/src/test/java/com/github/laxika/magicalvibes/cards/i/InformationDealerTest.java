package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InformationDealer.class, Forest.class, Island.class, Mountain.class, Plains.class})
class InformationDealerTest extends BaseCardTest {

    @Test
    void countsWizardsOnBothBattlefieldsWhenAbilityResolves() {
        Permanent informationDealer = harness.addToBattlefieldAndReturn(player1, new InformationDealer());
        informationDealer.setSummoningSick(false);
        Card topCard = new Forest();
        Card secondCard = new Island();
        Card thirdCard = new Mountain();
        Card fourthCard = new Plains();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard, fourthCard));

        harness.activateAbility(player1, 0, null, null);
        Permanent opponentWizard = harness.addToBattlefieldAndReturn(player2, new InformationDealer());
        opponentWizard.setSummoningSick(false);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(secondCard, topCard, thirdCard, fourthCard);
    }

    @Test
    void looksAtOnlyOneCardWithOnlyTheSourceWizard() {
        Permanent informationDealer = harness.addToBattlefieldAndReturn(player1, new InformationDealer());
        informationDealer.setSummoningSick(false);
        Card topCard = new Forest();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
