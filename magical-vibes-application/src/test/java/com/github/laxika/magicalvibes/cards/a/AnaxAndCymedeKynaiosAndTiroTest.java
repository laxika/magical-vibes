package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnaxAndCymedeKynaiosAndTiro.class, Shock.class, Forest.class, GrizzlyBears.class})
class AnaxAndCymedeKynaiosAndTiroTest extends BaseCardTest {

    @Test
    void drawsAndPutsChosenLandsTogetherThenDrawsForDecliningOpponents() {
        Permanent source = harness.addToBattlefieldAndReturn(player1,
                new AnaxAndCymedeKynaiosAndTiro());
        Forest playerLand = new Forest();
        Forest opponentLand = new Forest();
        GrizzlyBears playerDraw = new GrizzlyBears();
        GrizzlyBears opponentDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(new Shock(), playerLand));
        harness.setHand(player2, List.of(opponentLand));
        harness.setLibrary(player1, List.of(playerDraw));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice firstChoice =
                gd.interaction.activeInteraction(
                        PendingInteraction.EachPlayerMayPutLandFromHandThenOpponentsDrawChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(playerLand.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(playerLand.getId()));

        harness.handleMultipleCardsChosen(player2, List.of());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .contains("Forest");
    }

    @Test
    void opponentWhoPutsALandDoesNotDraw() {
        Permanent source = harness.addToBattlefieldAndReturn(player1,
                new AnaxAndCymedeKynaiosAndTiro());
        Forest opponentLand = new Forest();
        GrizzlyBears opponentDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(opponentLand));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(opponentLand.getId()));

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }
}
