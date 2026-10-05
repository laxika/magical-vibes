package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.cards.r.RuptureSpire;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MatcaRioters.class, Forest.class, Island.class, Plains.class, Swamp.class, Mountain.class,
        PrismaticOmen.class, RuptureSpire.class})
class MatcaRiotersTest extends BaseCardTest {

    @Test
    @DisplayName("Matca Rioters power and toughness equal the number of basic land types you control")
    void ptEqualsDomain() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(3);
    }

    @Test
    @DisplayName("Matca Rioters counts each basic land type only once")
    void countsEachTypeOnce() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(2);
    }

    @Test
    @DisplayName("Matca Rioters counts only your lands, not opponent lands")
    void countsOnlyControllersLands() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(1);
    }

    @Test
    @DisplayName("Matca Rioters is 0/0 with no basic lands")
    void zeroWithNoBasicLands() {
        Permanent rioters = addRiotersReady(player1);

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(0);
    }

    @Test
    @DisplayName("Matca Rioters counts land types granted by Prismatic Omen")
    void countsGrantedBasicLandTypes() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new PrismaticOmen());

        // The lone Forest is every basic land type in addition to its own, so domain is 5.
        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(5);
    }

    @Test
    @DisplayName("Matca Rioters P/T updates when lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(1);

        harness.addToBattlefield(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(2);
    }

    @Test
    @DisplayName("Matca Rioters resolves from the stack and uses its controller's domain")
    void resolvesFromStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.castFromHand(player1, new MatcaRioters(), "{2}{G}");
        harness.passBothPriorities();

        Permanent rioters = findPermanent(player1, "Matca Rioters");
        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing the last land sends Matca Rioters to the graveyard")
    void diesWhenLastBasicLandTypeIsLost() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent rioters = addRiotersReady(player1);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        assertThat(gqs.getEffectivePower(gd, rioters)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isZero();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Matca Rioters");
        harness.assertInGraveyard(player1, "Matca Rioters");
    }

    @Test
    @DisplayName("A nonbasic land without basic land types contributes nothing")
    void untypedNonbasicLandDoesNotContribute() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new RuptureSpire());
        Permanent rioters = addRiotersReady(player1);

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prismatic Omen makes a nonbasic land contribute all five types")
    void nonbasicLandWithGrantedTypesContributes() {
        harness.addToBattlefield(player1, new RuptureSpire());
        harness.addToBattlefield(player1, new PrismaticOmen());
        Permanent rioters = addRiotersReady(player1);

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(5);
    }

    @Test
    @DisplayName("Matca Rioters uses its owner's domain in hand and graveyard")
    void domainAppliesOutsideBattlefield() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Island());
        MatcaRioters rioters = new MatcaRioters();
        harness.setHand(player1, List.of(rioters));

        assertThat(gqs.getEffectiveCardPower(gd, rioters)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, rioters)).isEqualTo(2);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(rioters));
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectiveCardPower(gd, rioters)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, rioters)).isEqualTo(3);
    }

    private Permanent addRiotersReady(Player player) {
        MatcaRioters card = new MatcaRioters();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
