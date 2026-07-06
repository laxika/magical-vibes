package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Counter trigger")
    class CounterTriggerTests {

        @Test
        @DisplayName("Increment putting a +1/+1 counter triggers mana ability")
        void incrementAddsCounterAndTriggersMana() {
            Permanent berta = addBerta(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // Increment resolves and puts a counter on Berta
            harness.passBothPriorities(); // Berta mana trigger resolves

            GameData localGd = harness.getGameData();
            assertThat(berta.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(localGd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

            int before = localGd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
            harness.handleListChoice(player1, "BLUE");
            assertThat(localGd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        }
    }

    @Nested
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

            GameData localGd = harness.getGameData();
            List<Permanent> fractals = localGd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().isToken() && "Fractal".equals(p.getCard().getName()))
                    .toList();
            assertThat(fractals).hasSize(1);

            Permanent fractal = fractals.getFirst();
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
            assertThat(fractal.getEffectivePower()).isEqualTo(3);
            assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
            assertThat(fractal.getCard().getSubtypes()).contains(CardSubtype.FRACTAL);
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
                    .anyMatch(c -> c.isToken() && "Fractal".equals(c.getName()));
        }
    }
}
