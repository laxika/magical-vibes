package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.b.Blightbeetle;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OmnathLocusOfTheRoil.class, AirElemental.class, Forest.class, GreenwoodSentinel.class})
class OmnathLocusOfTheRoilTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter trigger deals damage equal to the number of Elementals controlled")
    void enterTriggerCountsItselfAndOtherElementals() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new AirElemental());

        castOmnath();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on a targeted Elemental and draws at eight lands")
    void landfallCountersTargetAndDrawsAtEightLands() {
        Permanent elemental = addOmnathAndElemental(7);
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Landfall still counters an Elemental below eight lands without drawing")
    void landfallBelowEightLandsDoesNotDraw() {
        Permanent elemental = addOmnathAndElemental(0);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Landfall cannot target a non-Elemental creature")
    void landfallRejectsNonElementalTarget() {
        Permanent nonElemental = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        addOmnathAndElemental(0);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonElemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enterTriggerCanDamagePlayerAndCountsElementalsAtResolution() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AirElemental());
        castOmnath();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player1, new AirElemental());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    void enterTriggerCanDamageBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player1.getId());
        castOmnath();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    void landfallRejectsOpponentsElemental() {
        Permanent opponentElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        addOmnathAndElemental(0);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentElemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landfallCanTargetOmnathItself() {
        Permanent omnath = harness.addToBattlefieldAndReturn(player1, new OmnathLocusOfTheRoil());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, omnath.getId());
        harness.passBothPriorities();

        assertThat(omnath.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent elemental = addOmnathAndElemental(0);
        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landfallDoesNotDrawWhenItsOnlyTargetLeavesBattlefield() {
        Permanent elemental = addOmnathAndElemental(7);
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        gd.playerBattlefields.get(player1.getId()).remove(elemental);
        gd.playerGraveyards.get(player1.getId()).add(elemental.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void landfallChecksLandThresholdAtResolutionRatherThanTriggerTime() {
        Permanent elemental = addOmnathAndElemental(6);
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Greenwood Sentinel");
    }

    @Test
    void landfallDoesNotDrawIfLandCountDropsBelowEightBeforeResolution() {
        Permanent elemental = addOmnathAndElemental(7);
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(Blightbeetle.class)
    void landfallDrawsEvenWhenCounterPlacementIsPrevented() {
        Permanent elemental = addOmnathAndElemental(7);
        harness.addToBattlefield(player2, new Blightbeetle());
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Greenwood Sentinel");
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Lignify.class})
    void landfallCanTargetNoncreatureElementalPermanent() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Lignify());
        aura.setAttachedTo(host.getId());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "TREEFOLK");
        harness.handleListChoice(player1, "ELEMENTAL");
        harness.addToBattlefield(player1, new OmnathLocusOfTheRoil());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addOmnathAndElemental(int landCount) {
        harness.addToBattlefield(player1, new OmnathLocusOfTheRoil());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        for (int i = 0; i < landCount; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        return elemental;
    }

    private void castOmnath() {
        harness.castFromHand(player1, new OmnathLocusOfTheRoil(), "{1}{G}{U}{R}");
    }
}
