package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BullHippo;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoeticScales.class, CoralMerfolk.class, BullHippo.class, Forest.class})
class NoeticScalesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns only the active player's creatures with power greater than their hand size")
    void returnsQualifyingActivePlayerCreatures() {
        harness.addToBattlefield(player1, new NoeticScales());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent bullHippo = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        Permanent opponentBullHippo = harness.addToBattlefieldAndReturn(player2, new BullHippo());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merfolk).doesNotContain(bullHippo);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentBullHippo);
        assertThat(gd.playerHands.get(player1.getId())).contains(bullHippo.getCard());
    }

    @Test
    @DisplayName("Triggers during an opponent's upkeep")
    void triggersDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new NoeticScales());
        Permanent bullHippo = harness.addToBattlefieldAndReturn(player2, new BullHippo());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bullHippo);
        assertThat(gd.playerHands.get(player2.getId())).contains(bullHippo.getCard());
    }

    @Test
    @DisplayName("Uses the hand size when the trigger resolves")
    void usesCurrentHandSizeAtResolution() {
        harness.addToBattlefield(player1, new NoeticScales());
        Permanent bullHippo = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bullHippo);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bullHippo.getCard());
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's hand")
    void returnsControlledCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new NoeticScales());
        Permanent stolenBullHippo = harness.addToBattlefieldAndReturn(player2, new BullHippo());
        gd.stolenCreatures.put(stolenBullHippo.getId(), player1.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(stolenBullHippo);
        assertThat(gd.playerHands.get(player1.getId())).contains(stolenBullHippo.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(stolenBullHippo.getCard());
    }
}
