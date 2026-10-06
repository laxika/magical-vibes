package com.github.laxika.magicalvibes.cards.h;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HungryGraffalon.class, Concentrate.class, Shock.class, AirElemental.class})
class HungryGraffalonTest extends BaseCardTest {

    private Permanent addGraffalon(Player player) {
        return addCreatureReady(player, new HungryGraffalon());
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Nested
    @DisplayName("Increment")
    @CardUsed({HungryGraffalon.class, Concentrate.class, Shock.class})
    class IncrementTests {

        @Test
        @DisplayName("Casting a four-mana spell puts a +1/+1 counter on the 3/4")
        void fourManaSpellAddsCounter() {
            Permanent graffalon = addGraffalon(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.BLUE, 4);
            harness.setHand(player1, List.of(new Concentrate()));
            harness.castAndResolveSorcery(player1, 0, 0);

            assertThat(graffalon.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Casting a one-mana spell does not put a counter on the 3/4")
        void oneManaSpellAddsNoCounter() {
            Permanent graffalon = addGraffalon(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new Shock()));
            harness.castAndResolveInstant(player1, 0, player2.getId());

            assertThat(graffalon.getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        void manaEqualToPowerDoesNotTriggerWhenToughnessIsGreater() {
            Permanent graffalon = addGraffalon(player1);
            graffalon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            setUpMainPhase(player1);
            harness.addMana(player1, ManaColor.GREEN, 4);
            harness.setHand(player1, List.of(new HungryGraffalon()));

            harness.castCreature(player1, 0);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            assertThat(graffalon.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        void greaterThanToughnessAloneIsEnough() {
            Permanent graffalon = addGraffalon(player1);
            graffalon.setPowerModifier(2);
            graffalon.setToughnessModifier(-1);
            setUpMainPhase(player1);
            harness.addMana(player1, ManaColor.GREEN, 4);
            harness.setHand(player1, List.of(new HungryGraffalon()));

            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            assertThat(graffalon.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        void opponentSpellDoesNotTriggerIncrement() {
            Permanent graffalon = addGraffalon(player1);
            setUpMainPhase(player2);
            harness.addMana(player2, ManaColor.GREEN, 4);
            harness.setHand(player2, List.of(new HungryGraffalon()));

            harness.castCreature(player2, 0);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            assertThat(graffalon.getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        void conditionIsCheckedAgainAtResolution() {
            Permanent graffalon = addGraffalon(player1);
            setUpMainPhase(player1);
            harness.addMana(player1, ManaColor.GREEN, 4);
            harness.setHand(player1, List.of(new HungryGraffalon()));
            harness.castCreature(player1, 0);
            assertThat(gd.stack).hasSize(2);

            graffalon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            harness.passBothPriorities();

            assertThat(graffalon.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        void spellDoesNotTriggerItsOwnIncrementAsItEnters() {
            setUpMainPhase(player1);
            harness.addMana(player1, ManaColor.GREEN, 4);
            harness.setHand(player1, List.of(new HungryGraffalon()));

            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                    .satisfies(permanent -> assertThat(permanent.getPlusOnePlusOneCounters()).isZero());
        }
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        Permanent graffalon = addGraffalon(player2);
        addCreatureReady(player1, new AirElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(graffalon.isBlocking()).isTrue();
    }
}
