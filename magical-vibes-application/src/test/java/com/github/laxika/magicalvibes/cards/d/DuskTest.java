package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dusk.class, Dawn.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class DuskTest extends BaseCardTest {

    @Test
    @DisplayName("Dusk destroys creatures with power 3 or greater")
    void duskDestroysCreaturesWithPowerAtLeastThree() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Dusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Dawn returns all low-power creature cards to hand, then exiles Dusk")
    void dawnReturnsLowPowerCreaturesAndExilesParentCard() {
        Card lowPowerCreature = new GrizzlyBears();
        Card highPowerCreature = new HillGiant();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(new Dusk(), lowPowerCreature, highPowerCreature, land));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .containsExactly(highPowerCreature, land);
        assertThat(gameData.playerHands.get(player1.getId()))
                .contains(lowPowerCreature);
        assertThat(gameData.playerHands.get(player1.getId()))
                .doesNotContain(highPowerCreature, land);
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dusk"));
    }

    @Test
    @DisplayName("Dusk uses current power rather than printed power")
    void duskUsesCurrentPower() {
        Permanent boostedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        boostedBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent weakenedGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        weakenedGiant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Dusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Dusk");
    }

    @Test
    @DisplayName("Dawn returns every eligible card only from its controller's graveyard")
    void dawnReturnsAllOnlyFromControllersGraveyard() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card opponentsBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Dusk(), firstBear, secondBear));
        harness.setGraveyard(player2, List.of(opponentsBear));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstBear, secondBear);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsBear);
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dawn still exiles the card when there are no eligible creatures")
    void dawnExilesWithNoEligibleCreatures() {
        Card giant = new HillGiant();
        Card dusk = new Dusk();
        harness.setGraveyard(player1, List.of(dusk, giant));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(giant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(dusk);
        harness.assertNotInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Dawn returns a creature destroyed by Dusk after its power counters cease to apply")
    void dawnReturnsCreatureWhoseCountersMadeItEligibleForDusk() {
        Card bear = new GrizzlyBears();
        Card giant = new HillGiant();
        Card dusk = new Dusk();
        harness.addToBattlefieldAndReturn(player1, bear)
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, giant);
        harness.setHand(player1, List.of(dusk));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear, giant, dusk);
        int duskIndex = gd.playerGraveyards.get(player1.getId()).indexOf(dusk);
        harness.castAndResolveFlashback(player1, duskIndex, null);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(giant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(dusk);
    }
}
