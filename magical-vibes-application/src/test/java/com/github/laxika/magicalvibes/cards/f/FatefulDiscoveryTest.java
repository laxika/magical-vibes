package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatefulDiscovery.class, Forest.class, Ornithopter.class})
class FatefulDiscoveryTest extends BaseCardTest {

    @Test
    void artifactEnteringUnderYourControlDrawsACard() {
        harness.addToBattlefield(player1, new FatefulDiscovery());
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(drawn));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void artifactEnteringUnderAnOpponentsControlDoesNotDrawACard() {
        harness.addToBattlefield(player1, new FatefulDiscovery());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }
}
