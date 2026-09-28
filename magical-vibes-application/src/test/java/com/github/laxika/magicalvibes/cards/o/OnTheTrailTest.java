package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnTheTrail.class, Forest.class, GrizzlyBears.class})
class OnTheTrailTest extends BaseCardTest {

    @Test
    @DisplayName("May put a land from hand onto the battlefield tapped after the second draw")
    void putsLandTappedAfterSecondDraw() {
        harness.addToBattlefield(player1, new OnTheTrail());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        setLibrary(player1, new GrizzlyBears(), new GrizzlyBears());

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card == forest)
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard() == forest && permanent.isTapped());
    }

    @Test
    @DisplayName("Declining the trigger leaves the land in hand")
    void decliningTriggerLeavesLandInHand() {
        harness.addToBattlefield(player1, new OnTheTrail());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        setLibrary(player1, new GrizzlyBears(), new GrizzlyBears());

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard() == forest);
    }

    @Test
    @DisplayName("Triggers only on the second card drawn each turn")
    void triggersOnlyOnSecondCardDraw() {
        harness.addToBattlefield(player1, new OnTheTrail());
        harness.setHand(player1, List.of(new Forest()));
        setLibrary(player1, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());

        drawCard(player1);
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void setLibrary(Player player, Card... cards) {
        harness.setLibrary(player, List.of(cards));
    }
}
