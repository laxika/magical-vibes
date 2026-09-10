package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornOfGreed;
import com.github.laxika.magicalvibes.cards.t.TatyovaBenthicDruid;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientGreenwarden.class, Forest.class, GrizzlyBears.class, HornOfGreed.class,
        TatyovaBenthicDruid.class})
class AncientGreenwardenTest extends BaseCardTest {

    @Test
    void controllerMayPlayLandsFromGraveyard() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void doublesLandfallTriggerFromLandEnteringUnderControllerControl() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void doublesTriggerOfPermanentYouControlWhenOpponentPlaysLand() {
        harness.addToBattlefield(player1, new AncientGreenwarden());
        harness.addToBattlefield(player1, new HornOfGreed());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
