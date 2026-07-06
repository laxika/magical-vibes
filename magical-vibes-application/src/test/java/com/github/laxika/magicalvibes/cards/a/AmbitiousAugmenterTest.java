package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmbitiousAugmenterTest extends BaseCardTest {

    private Permanent addAugmenter(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AmbitiousAugmenter());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    

    @Nested
    @DisplayName("Increment")
    class IncrementTests {

        @Test
        @DisplayName("Casting a two-mana spell puts a +1/+1 counter on the 1/1 (2 > 1)")
        void twoManaSpellAddsCounter() {
            Permanent augmenter = addAugmenter(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            assertThat(augmenter.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Casting a one-mana spell adds no counter (1 is not greater than power or toughness)")
        void oneManaSpellAddsNoCounter() {
            Permanent augmenter = addAugmenter(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new Shock()));
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(augmenter.getPlusOnePlusOneCounters()).isZero();
        }

        @Test
        @DisplayName("Increment compares against the creature's current power/toughness")
        void comparesAgainstCurrentStats() {
            Permanent augmenter = addAugmenter(player1);
            augmenter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // now a 2/2
            setUpMainPhase(player1);

            // Two-mana spell: 2 is not greater than 2 power or 2 toughness -> no counter.
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            assertThat(augmenter.getPlusOnePlusOneCounters()).isEqualTo(1);

            // Three-mana spell ({2}{G} Hurricane with X=2): 3 > 2 -> counter added.
            harness.addMana(player1, ManaColor.GREEN, 3);
            harness.setHand(player1, List.of(new Hurricane()));
            harness.castSorcery(player1, 0, 2);
            harness.passBothPriorities();
            assertThat(augmenter.getPlusOnePlusOneCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("Increment triggers on power OR toughness — mana greater than power alone suffices")
        void powerOrToughness() {
            Permanent augmenter = addAugmenter(player1);
            augmenter.setToughnessModifier(2); // now a 1/3
            setUpMainPhase(player1);

            // Two-mana spell: 2 > power (1) even though 2 is not greater than toughness (3).
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            assertThat(augmenter.getPlusOnePlusOneCounters()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        // The opponent casts the removal so Increment (a controller-casts-spell trigger) does not
        // fire and add counters before the augmenter dies.
        @Test
        @DisplayName("Dying with counters creates a Fractal token carrying those counters")
        void dyingWithCountersCreatesFractal() {
            Permanent augmenter = addAugmenter(player1);
            augmenter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Wrath resolves — augmenter dies, death trigger goes on stack
            harness.passBothPriorities(); // Death trigger resolves

            GameData gd = harness.getGameData();
            List<Permanent> fractals = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Fractal"))
                    .toList();
            assertThat(fractals).hasSize(1);

            Permanent fractal = fractals.getFirst();
            assertThat(fractal.getPlusOnePlusOneCounters()).isEqualTo(2);
            assertThat(fractal.getEffectivePower()).isEqualTo(2);
            assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
            assertThat(fractal.getCard().isToken()).isTrue();
        }

        @Test
        @DisplayName("Dying with no counters creates no token (intervening-if fails)")
        void dyingWithoutCountersCreatesNoToken() {
            addAugmenter(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Wrath resolves — augmenter dies, no death trigger fires

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fractal"));
        }
    }
}
