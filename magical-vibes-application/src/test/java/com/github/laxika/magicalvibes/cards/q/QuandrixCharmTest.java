package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuandrixCharmTest extends BaseCardTest {

    private void addGU() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("Has a ChooseOneEffect with three options")
    void hasCorrectEffects() {
        QuandrixCharm card = new QuandrixCharm();

        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.options()).hasSize(3);
        assertThat(effect.options().get(0).effect()).isInstanceOf(CounterUnlessPaysEffect.class);
        assertThat(effect.options().get(1).effect()).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(effect.options().get(2).effect()).isInstanceOf(SetBasePowerToughnessUntilEndOfTurnEffect.class);
        assertThat(((CounterUnlessPaysEffect) effect.options().get(0).effect()).amount()).isEqualTo(2);
    }

    @Nested
    @DisplayName("Mode 0: Counter target spell unless controller pays {2}")
    class CounterMode {

        @Test
        @DisplayName("Counters when opponent cannot pay {2}")
        void countersWhenCannotPay() {
            harness.forceActivePlayer(player2);
            LlanowarElves elves = new LlanowarElves();
            harness.setHand(player2, List.of(elves));
            harness.addMana(player2, ManaColor.GREEN, 1);

            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            harness.castCreature(player2, 0);
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, elves.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Llanowar Elves"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Glorious Anthem"));
        }

        @Test
        @DisplayName("Cannot target a creature with the enchantment mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new GloriousAnthem());
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature has base P/T 5/5 until end of turn")
    class BasePowerToughnessMode {

        @Test
        @DisplayName("Sets base power and toughness to 5/5")
        void setsBaseFiveFive() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bear.isBasePowerToughnessOverriddenUntilEndOfTurn()).isTrue();
            assertThat(bear.getEffectivePower()).isEqualTo(5);
            assertThat(bear.getEffectiveToughness()).isEqualTo(5);
        }

        @Test
        @DisplayName("Wears off at cleanup")
        void wearsOffAtCleanup() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bear.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
            assertThat(bear.getEffectivePower()).isEqualTo(2);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target an enchantment with the creature mode")
        void cannotTargetEnchantment() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
