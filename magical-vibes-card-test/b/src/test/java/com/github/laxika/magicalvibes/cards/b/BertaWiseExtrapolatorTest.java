package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.l.LumaretsFavor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BertaWiseExtrapolator.class, GrizzlyBears.class, LumaretsFavor.class, DoublingSeason.class})
class BertaWiseExtrapolatorTest extends BaseCardTest {

    private Permanent addBerta(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BertaWiseExtrapolator());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Nested
    @CardUsed({BertaWiseExtrapolator.class, GrizzlyBears.class, LumaretsFavor.class, DoublingSeason.class})
    @DisplayName("Counter trigger")
    class CounterTriggerTests {

        @Test
        @DisplayName("Increment putting a +1/+1 counter triggers the mana-producing ability")
        void incrementAddsCounterAndTriggersMana() {
            Permanent berta = addBerta(player1);
            setUpMainPhase(player1);

            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            harness.passBothPriorities(); // Increment resolves and puts a counter on Berta
            harness.passBothPriorities(); // Berta mana trigger resolves

            GameData localGd = harness.getGameData();
            assertThat(berta.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(localGd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

            int before = localGd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
            harness.handleListChoice(player1, "BLUE");
            assertThat(localGd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        }

        @Test
        void equalManaSpentDoesNotIncrement() {
            Permanent berta = addBerta(player1);
            berta.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            setUpMainPhase(player1);

            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            resolveAllTriggers();

            assertThat(berta.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        void opponentsSpellDoesNotIncrement() {
            Permanent berta = addBerta(player1);
            setUpMainPhase(player2);

            harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
            resolveAllTriggers();

            assertThat(berta.getPlusOnePlusOneCounters()).isZero();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        void incrementRechecksPowerAndToughnessOnResolution() {
            Permanent berta = addBerta(player1);
            setUpMainPhase(player1);
            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

            harness.setHand(player2, List.of(new LumaretsFavor()));
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.castInstant(player2, 0, berta.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(berta.getPlusOnePlusOneCounters()).isZero();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        void multipleCountersFromOneEventProduceOnlyOneMana() {
            Permanent berta = addBerta(player1);
            harness.addToBattlefield(player1, new DoublingSeason());
            setUpMainPhase(player1);

            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            harness.passBothPriorities();
            assertThat(berta.getPlusOnePlusOneCounters()).isEqualTo(2);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            harness.passBothPriorities();
            harness.handleListChoice(player1, "RED");

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    @Nested
    @CardUsed({BertaWiseExtrapolator.class, DoublingSeason.class})
    @DisplayName("Activated ability")
    class ActivatedAbilityTests {

        @Test
        @DisplayName("Paying X=3 creates a 3/3 Fractal token")
        void createsFractalWithXCounters() {
            addBerta(player1);
            setUpMainPhase(player1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateAbility(player1, 0, 3, null);
            harness.passBothPriorities();

            List<Permanent> fractals = findPermanents(player1, "Fractal");
            assertThat(fractals).hasSize(1);

            Permanent fractal = fractals.getFirst();
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
            assertThat(fractal.getEffectivePower()).isEqualTo(3);
            assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
            assertThat(fractal.getCard().getSubtypes()).contains(CardSubtype.FRACTAL);
        }

        @Test
        void doublingSeasonPutsCountersOnEveryCreatedFractal() {
            addBerta(player1);
            harness.addToBattlefield(player1, new DoublingSeason());
            setUpMainPhase(player1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateAbility(player1, 0, 3, null);
            harness.passBothPriorities();

            assertThat(findPermanents(player1, "Fractal")).hasSize(2)
                    .allSatisfy(fractal -> {
                        assertThat(fractal.getPlusOnePlusOneCounters()).isEqualTo(6);
                        assertThat(fractal.getEffectivePower()).isEqualTo(6);
                        assertThat(fractal.getEffectiveToughness()).isEqualTo(6);
                    });
        }

        @Test
        @DisplayName("Paying X=0 creates a 0/0 Fractal token that dies to state-based actions")
        void xZeroCreatesZeroZeroFractal() {
            addBerta(player1);
            setUpMainPhase(player1);

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();

            GameData localGd = harness.getGameData();
            assertThat(localGd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().isToken() && "Fractal".equals(p.getCard().getName()));
            assertThat(localGd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.isToken() && "Fractal".equals(c.getName()));
            assertThat(localGd.gameLog.stream().map(entry -> entry.plainText()))
                    .anyMatch(log -> log.contains("0/0") && log.contains("Fractal")
                            && log.contains("enters the battlefield"))
                    .anyMatch(log -> log.contains("Fractal") && log.contains("ceases to exist"));
        }
    }
}
