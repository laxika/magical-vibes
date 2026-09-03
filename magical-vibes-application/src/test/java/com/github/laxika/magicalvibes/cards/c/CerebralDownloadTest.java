package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CerebralDownload.class, Forest.class, Ornithopter.class})
class CerebralDownloadTest extends BaseCardTest {

    @Test
    void surveilsForControlledArtifactsThenDrawsThree() {
        Card surveiled = new Forest();
        Card keptOnTop = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card thirdDraw = new Forest();
        harness.setLibrary(player1, List.of(surveiled, keptOnTop, firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(surveiled, keptOnTop);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptOnTop, firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveiled);
    }

    @Test
    void drawsThreeWithoutSurveillingWhenNoArtifactsAreControlled() {
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card thirdDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firstDraw, secondDraw, thirdDraw);
    }
}
