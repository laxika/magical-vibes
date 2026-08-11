package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionOfThePathTest extends BaseCardTest {

    @Test
    @DisplayName("Beholds an Elemental and returns it to its owner's hand when Champion leaves")
    void beholdsElementalAndReturnsItToHand() {
        Card beheldCard = new AirElemental();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.setHand(player1, List.of(new ChampionOfThePath()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Champion of the Path");
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champion));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Another Elemental deals damage equal to its power to each opponent")
    void anotherElementalDealsItsPowerToEachOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChampionOfThePath());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteringElemental = findPermanent(player1, "Air Elemental");
        int enteringPower = gqs.getEffectivePower(gd, enteringElemental);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20 - enteringPower);
    }

    @Test
    @DisplayName("A non-Elemental creature does not trigger the damage ability")
    void nonElementalDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChampionOfThePath());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
