package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonologueTax.class, LightningBolt.class})
class MonologueTaxTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when an opponent casts their second spell")
    void createsTreasureOnOpponentsSecondSpell() {
        harness.addToBattlefield(player1, new MonologueTax());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        prepareOpponentMainPhase();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger on the controller's second spell")
    void doesNotTriggerOnControllersSecondSpell() {
        harness.addToBattlefield(player1, new MonologueTax());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
