package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabasterPotion.class, GrizzlyBears.class, LightningBolt.class, ChandraNalaar.class})
class AlabasterPotionTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target player gains X life")
    class GainLifeMode {

        @Test
        void zeroXGainsNoLife() {
            harness.setLife(player1, 12);
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castModalInstantForX(player1, 0, 0, 0, player1.getId());
            harness.passBothPriorities();

            harness.assertLife(player1, 12);
            harness.assertInGraveyard(player1, "Alabaster Potion");
        }

        @Test
        @DisplayName("Target player gains X life for X paid")
        void targetPlayerGainsXLife() {
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);
            int before = gd.playerLifeTotals.get(player2.getId());

            harness.castModalInstantForX(player1, 0, 0, 3, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, before + 3);
        }

        @Test
        @DisplayName("Cannot target a creature with the gain-life mode")
        void cannotTargetCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            assertThatThrownBy(() -> harness.castModalInstantForX(player1, 0, 0, 3, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Prevent the next X damage to any target")
    class PreventDamageMode {

        @Test
        void unusedPreventionCarriesOverToTheNextDamageEvent() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new AlabasterPotion(), new LightningBolt(), new LightningBolt()));
            harness.addMana(player1, ManaColor.WHITE, 6);
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castModalInstantForX(player1, 0, 1, 4, player2.getId());
            harness.passBothPriorities();
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            harness.assertLife(player2, 20);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            harness.assertLife(player2, 18);
        }

        @Test
        void creatureSurvivesWhenOnlyOneDamageGetsThrough() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion(), new LightningBolt()));
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castModalInstantForX(player1, 0, 1, 2, bears.getId());
            harness.passBothPriorities();
            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(bears.getDamagePreventionShield()).isZero();
        }

        @Test
        void zeroXPreventsNoDamage() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new AlabasterPotion(), new LightningBolt()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castModalInstantForX(player1, 0, 1, 0, player2.getId());
            harness.passBothPriorities();
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 17);
        }

        @Test
        void unusedShieldExpiresAtEndOfTurn() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);
            harness.castModalInstantForX(player1, 0, 1, 3, player2.getId());
            harness.passBothPriorities();

            harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.castInstant(player2, 0, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("Adds an X-damage prevention shield to a target creature")
        void shieldOnCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 2, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
        }

        @Test
        @DisplayName("Adds an X-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 3, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }

        @Test
        @DisplayName("Prevents combat damage to the targeted player")
        void preventsCombatDamageToTargetPlayer() {
            harness.setLife(player2, 20);
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 2, player2.getId());
            harness.passBothPriorities();

            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
            resolveCombat();

            harness.assertLife(player2, 20);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("Prevents damage to a targeted planeswalker")
        void preventsDamageToTargetPlaneswalker() {
            Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
            chandra.setCounterCount(CounterType.LOYALTY, 6);
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 3, chandra.getId());
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, chandra.getId());
            harness.passBothPriorities();

            assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        }

        @Test
        @DisplayName("Prevents only the next X noncombat damage to the targeted player")
        void preventsOnlyNextXNoncombatDamage() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castModalInstantForX(player1, 0, 1, 2, player2.getId());
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 19);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }
    }
}
