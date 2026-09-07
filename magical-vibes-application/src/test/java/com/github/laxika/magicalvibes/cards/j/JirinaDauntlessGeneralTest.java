package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JirinaDauntlessGeneral.class, EliteVanguard.class, GrizzlyBears.class})
class JirinaDauntlessGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("When Jirina enters, it exiles target player's graveyard")
    void exilesTargetPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing Jirina protects your Humans until end of turn")
    void sacrificesAndProtectsYourHumans() {
        harness.addToBattlefield(player1, new JirinaDauntlessGeneral());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Jirina, Dauntless General");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
