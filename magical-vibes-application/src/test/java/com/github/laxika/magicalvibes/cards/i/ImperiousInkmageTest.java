package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImperiousInkmageTest extends BaseCardTest {

    private GameData resolveEtbSurveil() {
        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new ImperiousInkmage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd;
    }

    private Card[] seedTopTwo(GameData gd) {
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.add(0, top1);
        deck.add(0, top0);
        return new Card[]{top0, top1};
    }

    @Test
    @DisplayName("ETB creates a surveil 2 interaction")
    void etbEntersSurveilState() {
        GameData gd = resolveEtbSurveil();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).hasSize(2);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Surveil 2 can keep one card on top and put the other into the graveyard")
    void surveilSplitsTopAndGraveyard() {
        GameData gd = harness.getGameData();
        Card[] top = seedTopTwo(gd);

        resolveEtbSurveil();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(top[1]);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top[0]);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top[1]);
    }
}
