package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JeskaiCharmTest extends BaseCardTest {

    private void addJeskaiMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Put target creature on top of its owner's library")
    class TuckMode {

        @Test
        @DisplayName("Puts the target creature on top of its owner's library")
        void putsCreatureOnTopOfLibrary() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new JeskaiCharm()));
            addJeskaiMana();

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            List<Card> deck = gd.playerDecks.get(player2.getId());
            assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player2, new ChandraNalaar());
            harness.setHand(player1, List.of(new JeskaiCharm()));
            addJeskaiMana();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0,
                    harness.getPermanentId(player2, "Chandra Nalaar")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Jeskai Charm deals 4 damage to target opponent or planeswalker")
    class DamageMode {

        @Test
        @DisplayName("Deals 4 damage to the target opponent")
        void dealsDamageToOpponent() {
            harness.setHand(player1, List.of(new JeskaiCharm()));
            addJeskaiMana();

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 16);
        }

        @Test
        @DisplayName("Deals 4 damage to a target planeswalker")
        void dealsDamageToPlaneswalker() {
            Permanent chandra = new Permanent(new ChandraNalaar());
            chandra.setCounterCount(CounterType.LOYALTY, 6);
            gd.playerBattlefields.get(player2.getId()).add(chandra);
            harness.setHand(player1, List.of(new JeskaiCharm()));
            addJeskaiMana();

            harness.castInstant(player1, 0, 1, chandra.getId());
            harness.passBothPriorities();

            assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target yourself")
        void cannotTargetSelf() {
            harness.setHand(player1, List.of(new JeskaiCharm()));
            addJeskaiMana();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player1.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Creatures you control get +1/+1 and gain lifelink until end of turn")
    class PumpMode {

        @Test
        @DisplayName("Boosts your creatures and grants lifelink only to them")
        void boostsOwnCreaturesAndGrantsLifelink() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new JeskaiCharm()));
            addJeskaiMana();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent ownBear = findPermanent(player1, "Grizzly Bears");
            Permanent opponentBear = findPermanent(player2, "Grizzly Bears");
            assertThat(ownBear.getEffectivePower()).isEqualTo(3);
            assertThat(ownBear.getEffectiveToughness()).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, ownBear, Keyword.LIFELINK)).isTrue();
            assertThat(opponentBear.getEffectivePower()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.LIFELINK)).isFalse();
        }

        @Test
        @DisplayName("Boost and lifelink wear off at end of turn")
        void effectsWearOffAtEndOfTurn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new JeskaiCharm()));
            addJeskaiMana();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();
            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bear = findPermanent(player1, "Grizzly Bears");
            assertThat(bear.getEffectivePower()).isEqualTo(2);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
        }
    }
}
