package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AftermathAnalyst.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Mountain.class})
class AftermathAnalystTest extends BaseCardTest {

    @Test
    void millsThreeCardsWhenItEnters() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new LightningBolt();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new AftermathAnalyst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void returnsAllLandsFromGraveyardTappedAfterSacrificingIt() {
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, nonland, mountain));
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new AftermathAnalyst());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(analyst), null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland, analyst.getCard());
        harness.assertNotOnBattlefield(player1, "Aftermath Analyst");
    }

    @Test
    void millsOnlyAvailableCardsWithoutMillingOpponent() {
        Card forest = new Forest();
        Card opponentCard = new Mountain();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new AftermathAnalyst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Aftermath Analyst");
    }

    @Test
    void sacrificesImmediatelyAndReturnsLandsPresentAtResolutionOnlyFromOwnGraveyard() {
        Card forest = new Forest();
        Card opponentLand = new Mountain();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentLand));
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new AftermathAnalyst());
        analyst.setSummoningSick(true);
        analyst.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(analyst), null, null);

        harness.assertNotOnBattlefield(player1, "Aftermath Analyst");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(analyst.getCard());
        gd.playerGraveyards.get(player1.getId()).add(forest);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(analyst.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    void canActivateWithNoLandsInGraveyard() {
        harness.setGraveyard(player1, List.of());
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new AftermathAnalyst());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(analyst), null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aftermath Analyst");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(analyst.getCard());
        assertThat(gd.stack).isEmpty();
    }
}
