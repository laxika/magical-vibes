package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringOverseer.class})
class InspiringOverseerTest extends BaseCardTest {

    @Test
    void entersAndItsTriggerGainsLifeAndDrawsACard() {
        harness.setHand(player1, List.of(new InspiringOverseer()));
        harness.setLibrary(player1, List.of(new InspiringOverseer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(InspiringOverseer.class);
    }

    @Test
    void lifeGainAndDrawWaitForTheSingleEntryTriggerToResolve() {
        InspiringOverseer drawnCard = new InspiringOverseer();
        harness.setHand(player1, List.of(new InspiringOverseer()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inspiring Overseer");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastBenefitsOnlyItsController() {
        InspiringOverseer drawnCard = new InspiringOverseer();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new InspiringOverseer());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }
}
