package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Scragnoth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuandrixCharm.class, GloriousAnthem.class, GrizzlyBears.class, LlanowarElves.class, Scragnoth.class})
class QuandrixCharmTest extends BaseCardTest {

    private void addGU() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Nested
    @CardUsed({QuandrixCharm.class, LlanowarElves.class, Scragnoth.class})
    @DisplayName("Mode 0: Counter target spell unless controller pays {2}")
    class CounterMode {

        @Test
        @DisplayName("Counters when opponent cannot pay {2}")
        void countersWhenCannotPay() {
            harness.forceActivePlayer(player2);
            LlanowarElves elves = new LlanowarElves();

            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            harness.castFromHand(player2, elves, "{G}");
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, elves.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Llanowar Elves");
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        }

        @Test
        void spellResolvesWhenControllerPaysTwoWithMixedColors() {
            harness.forceActivePlayer(player2);
            LlanowarElves elves = new LlanowarElves();
            harness.castFromHand(player2, elves, "{G}");
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.addMana(player2, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, elves.getId());
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player2, true);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Llanowar Elves");
            harness.assertNotInGraveyard(player2, "Llanowar Elves");
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        void controllerCanDeclinePaymentDespiteHavingEnoughMana() {
            harness.forceActivePlayer(player2);
            LlanowarElves elves = new LlanowarElves();
            harness.castFromHand(player2, elves, "{G}");
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, elves.getId());
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player2, false);

            harness.assertInGraveyard(player2, "Llanowar Elves");
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
        }

        @Test
        void controllerCanGeneratePaymentManaDuringResolution() {
            harness.forceActivePlayer(player2);
            for (int i = 0; i < 2; i++) {
                Permanent manaElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
                manaElf.setSummoningSick(false);
            }
            LlanowarElves spell = new LlanowarElves();
            harness.castFromHand(player2, spell, "{G}");
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, spell.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            gs.tapPermanent(gd, player2, 0);
            gs.tapPermanent(gd, player2, 1);
            harness.handleMayAbilityChosen(player2, true);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
            harness.assertNotInGraveyard(player2, "Llanowar Elves");
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        }

        @Test
        @CardUsed({QuandrixCharm.class, Scragnoth.class})
        void controllerMayPayEvenWhenSpellCannotBeCountered() {
            harness.forceActivePlayer(player2);
            Scragnoth spell = new Scragnoth();
            harness.castFromHand(player2, spell, "{4}{G}");
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, spell.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player2, true);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Scragnoth");
        }
    }

    @Nested
    @CardUsed({QuandrixCharm.class, GloriousAnthem.class, GrizzlyBears.class})
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

            harness.assertNotOnBattlefield(player2, "Glorious Anthem");
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
    @CardUsed({QuandrixCharm.class, GloriousAnthem.class, GrizzlyBears.class})
    @DisplayName("Mode 2: Target creature has base P/T 5/5 until end of turn")
    class BasePowerToughnessMode {

        @Test
        void baseSetterPreservesCountersAndAnthemBonusOnOpposingCreature() {
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            harness.addToBattlefield(player2, new GloriousAnthem());
            harness.setHand(player1, List.of(new QuandrixCharm()));
            addGU();

            harness.castInstant(player1, 0, 2, bear.getId());
            harness.passBothPriorities();

            assertThat(harness.getGameQueryService().getEffectivePower(gd, bear)).isEqualTo(8);
            assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bear)).isEqualTo(8);
        }

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
