package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Terravore.class, Plains.class, DuskImp.class})
class TerravoreTest extends BaseCardTest {

    @Test
    @DisplayName("Terravore is 0/0 with no land cards in any graveyard")
    void isZeroZeroWithNoLandsInGraveyards() {
        Permanent terravore = addCreatureReady(player1, new Terravore());

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(0);
    }

    @Test
    @DisplayName("Terravore counts land cards in all graveyards")
    void countsLandsInAllGraveyards() {
        Permanent terravore = addCreatureReady(player1, new Terravore());
        harness.setGraveyard(player1, List.of(new Plains(), new DuskImp()));
        harness.setGraveyard(player2, List.of(new Plains(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(3);
    }

    @Test
    @DisplayName("Terravore updates as land cards enter any graveyard")
    void updatesWhenGraveyardsChange() {
        Permanent terravore = addCreatureReady(player1, new Terravore());
        harness.setGraveyard(player1, List.of(new Plains()));

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(1);

        harness.setGraveyard(player2, List.of(new Plains()));

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(2);
    }

    @Test
    @DisplayName("Terravore's dynamic power and toughness stack with modifiers")
    void stacksWithModifiers() {
        Permanent terravore = addCreatureReady(player1, new Terravore());
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));

        terravore.setPowerModifier(2);
        terravore.setToughnessModifier(2);

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(4);
    }

    @Test
    @DisplayName("Terravore's dynamic power and toughness stack with +1/+1 counters")
    void stacksWithPlusOnePlusOneCounters() {
        Permanent terravore = addCreatureReady(player1, new Terravore());
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));

        terravore.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(3);
    }

    @Test
    @DisplayName("Terravore tramples over a blocker using its dynamic power")
    void trampleUsesDynamicPower() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Terravore());
        Permanent blocker = addCreatureReady(player2, new DuskImp());
        harness.setGraveyard(player1, List.of(new Plains(), new Plains(), new Plains()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Terravore shrinks when lands leave either graveyard")
    void shrinksWhenLandsLeaveGraveyards() {
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        harness.setGraveyard(player2, List.of(new Plains()));
        Permanent terravore = addCreatureReady(player1, new Terravore());

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setGraveyard(player2, List.of());

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(1);
    }

    @Test
    @DisplayName("Terravore dies after resolving with no lands in graveyards")
    void diesWhenCastWithNoLandsInGraveyards() {
        harness.castFromHand(player1, new Terravore(), "{1}{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Terravore");
        harness.assertInGraveyard(player1, "Terravore");
    }

    @Test
    @DisplayName("Terravore dies when the last land leaves all graveyards")
    void diesWhenLastLandLeavesGraveyards() {
        harness.setGraveyard(player2, List.of(new Plains()));
        addCreatureReady(player1, new Terravore());
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Terravore");

        harness.setGraveyard(player2, List.of());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Terravore");
        harness.assertInGraveyard(player1, "Terravore");
    }

    @Test
    @DisplayName("Terravore's power and toughness are defined in hand, graveyard, and exile")
    void definesPowerAndToughnessOutsideBattlefield() {
        Terravore inHand = new Terravore();
        Terravore inGraveyard = new Terravore();
        Terravore inExile = new Terravore();
        harness.setHand(player1, List.of(inHand, new Plains()));
        harness.setGraveyard(player1, List.of(inGraveyard, new Plains()));
        harness.setGraveyard(player2, List.of(new Plains(), new DuskImp()));
        harness.setExile(player1, List.of(inExile, new Plains()));
        harness.addToBattlefield(player2, new Plains());

        for (Terravore terravore : List.of(inHand, inGraveyard, inExile)) {
            assertThat(gqs.getEffectiveCardPower(gd, terravore)).isEqualTo(2);
            assertThat(gqs.getEffectiveCardToughness(gd, terravore)).isEqualTo(2);
        }

        harness.setGraveyard(player2, List.of());

        for (Terravore terravore : List.of(inHand, inGraveyard, inExile)) {
            assertThat(gqs.getEffectiveCardPower(gd, terravore)).isEqualTo(1);
            assertThat(gqs.getEffectiveCardToughness(gd, terravore)).isEqualTo(1);
        }
    }
}
