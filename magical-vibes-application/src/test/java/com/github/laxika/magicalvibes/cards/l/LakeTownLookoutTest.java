package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LakeTownLookout.class, Shock.class, Forest.class, GrizzlyBears.class})
class LakeTownLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, recruit creates a Soldier after discarding a nonland card")
    void diesAndRecruitsAfterNonlandDiscard() {
        Permanent lookout = prepareLookout(new GrizzlyBears(), new Forest());

        killLookout(lookout);

        harness.assertInGraveyard(player1, "Lake-town Lookout");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    @DisplayName("When it dies, recruit does not create a Soldier after discarding a land")
    void diesAndRecruitsAfterLandDiscard() {
        Permanent lookout = prepareLookout(new Forest(), new GrizzlyBears());

        killLookout(lookout);

        harness.assertInGraveyard(player1, "Lake-town Lookout");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    private Permanent prepareLookout(Card discardedCard, Card drawnCard) {
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        return harness.addToBattlefieldAndReturn(player1, new LakeTownLookout());
    }

    private void killLookout(Permanent lookout) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, lookout.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
