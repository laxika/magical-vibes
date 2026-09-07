package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenesisUltimatum.class, Forest.class, GrizzlyBears.class, HolyDay.class, MindStone.class, Shock.class})
class GenesisUltimatumTest extends BaseCardTest {

    @Test
    void putsChosenPermanentsOntoBattlefieldAndRestIntoHand() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card mindStone = new MindStone();
        Card holyDay = new HolyDay();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, holyDay, forest, shock, mindStone));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), forest.getId(), mindStone.getId());

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(bears.getId(), forest.getId(), mindStone.getId())));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(bears.getId(), forest.getId(), mindStone.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(holyDay.getId(), shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    @Test
    void mayChooseNoPermanentsAndPutAllFiveCardsIntoHand() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card holyDay = new HolyDay();
        Card mindStone = new MindStone();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, forest, holyDay, mindStone, shock));
        Card ultimatum = new GenesisUltimatum();
        cast(ultimatum);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(bears.getId(), forest.getId(), mindStone.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), forest.getId(), holyDay.getId(), mindStone.getId(), shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ultimatum);
    }

    private void cast(Card ultimatum) {
        harness.setHand(player1, List.of(ultimatum));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
