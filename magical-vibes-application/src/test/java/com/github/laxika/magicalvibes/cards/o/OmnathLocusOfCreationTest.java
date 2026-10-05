package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnathLocusOfCreation.class, AjaniOutlandChaperone.class, Forest.class, IntoTheRoil.class})
class OmnathLocusOfCreationTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsOnEnter() {
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new OmnathLocusOfCreation());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Landfall resolves its first three branches and then stops")
    void landfallBranchesByResolutionCount() {
        harness.addToBattlefield(player1, new OmnathLocusOfCreation());
        Permanent ownPlaneswalker = addPlaneswalker(player1, 5);
        Permanent opposingPlaneswalker = addPlaneswalker(player2, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveLandfall(new Forest());
        harness.assertLife(player1, 24);

        resolveLandfall(new Forest());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        resolveLandfall(new Forest());
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

        resolveLandfall(new Forest());
        harness.assertLife(player2, 16);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's lands do not trigger landfall")
    void opposingLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new OmnathLocusOfCreation());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        resolveLandfall(new Forest());
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Pending landfall triggers choose their branches when they resolve")
    void pendingTriggersCountResolutions() {
        harness.addToBattlefield(player1, new OmnathLocusOfCreation());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(3);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Third landfall resolution still deals damage after Omnath leaves")
    @CardUsed({IntoTheRoil.class})
    void thirdResolutionAfterSourceLeaves() {
        Permanent omnath = harness.addToBattlefieldAndReturn(player1, new OmnathLocusOfCreation());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        resolveLandfall(new Forest());
        resolveLandfall(new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, omnath.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Omnath, Locus of Creation");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Landfall resolution count resets on the opponent's turn")
    void resolutionCountResetsEachTurn() {
        harness.addToBattlefield(player1, new OmnathLocusOfCreation());
        harness.setLife(player1, 20);
        harness.setLibrary(player2, List.of(new Forest()));
        resolveLandfall(new Forest());
        resolveLandfall(new Forest());
        harness.assertLife(player1, 24);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        harness.assertLife(player1, 28);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private void resolveLandfall(Card land) {
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new AjaniOutlandChaperone());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }
}
