package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoeticScalesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns only the active player's creatures with power greater than their hand size")
    void returnsQualifyingActivePlayerCreatures() {
        harness.addToBattlefield(player1, new NoeticScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentAirElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(filler(), filler()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears).doesNotContain(airElemental);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentAirElemental);
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Triggers during an opponent's upkeep")
    void triggersDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new NoeticScales());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(airElemental);
        harness.assertInHand(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Uses the hand size when the trigger resolves")
    void usesCurrentHandSizeAtResolution() {
        harness.addToBattlefield(player1, new NoeticScales());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(filler(), filler(), filler(), filler()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(airElemental);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(airElemental.getCard());
    }

    private Card filler() {
        Card card = new Card();
        card.setName("Filler Card");
        card.setType(CardType.INSTANT);
        return card;
    }
}
