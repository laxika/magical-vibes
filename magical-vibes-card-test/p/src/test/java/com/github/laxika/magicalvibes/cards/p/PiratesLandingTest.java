package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiratesLanding.class, WilyGoblin.class, GrizzlyBears.class})
class PiratesLandingTest extends BaseCardTest {

    @Test
    void drawsACardWhenNoTreasureManaWasSpent() {
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new PiratesLanding()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void seeksAPirateInsteadOfDrawingWhenTreasureManaWasSpent() {
        Card soughtPirate = new WilyGoblin();
        Card nonPirate = new GrizzlyBears();
        harness.setHand(player1, List.of(new WilyGoblin(), new PiratesLanding()));
        harness.setLibrary(player1, List.of(nonPirate, soughtPirate));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(soughtPirate);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonPirate);
    }

    @Test
    void doesNotDrawWhenTreasureWasSpentButNoPirateCanBeSought() {
        Card nonPirate = new PiratesLanding();
        harness.setHand(player1, List.of(new PiratesLanding()));
        harness.setLibrary(player1, List.of(nonPirate));
        harness.enterBattlefieldAndReturn(player1, new WilyGoblin());
        harness.passBothPriorities();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonPirate);
    }

    @Test
    void drawsWhenTreasureManaWasProducedButNotSpentOnTheSpell() {
        Card drawnCard = new PiratesLanding();
        Card pirate = new WilyGoblin();
        harness.setHand(player1, List.of(new PiratesLanding()));
        harness.setLibrary(player1, List.of(drawnCard, pirate));
        harness.enterBattlefieldAndReturn(player1, new WilyGoblin());
        harness.passBothPriorities();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pirate);
    }
}
