package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.a.AjanisSunstriker;
import com.github.laxika.magicalvibes.cards.c.ChandrasSpitfire;
import com.github.laxika.magicalvibes.cards.f.FungusSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MagmaticGalleon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Damage to each creature (and each player): the creatures are found as the effect is applied (CR 608.2h), creatures
 * and players are dealt the damage in one damage event (one lifelink gain for the total, CR 120.3f; one trigger for
 * a player or permanent dealt damage), the dealer's deathtouch applies to each creature, and a face-down dealer is
 * named by no card (CR 708.2).
 */
@CardUsed({AjanisPridemate.class, AjanisSunstriker.class, ChandrasSpitfire.class, FungusSliver.class,
        GrizzlyBears.class, HillGiant.class, MagmaticGalleon.class, Shock.class, VampireNighthawk.class})
class MassDamageTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    private static CardEffect eachCreature(int damage) {
        return DealDamageEffect.toEachCreature(damage);
    }

    private static CardEffect eachCreatureAndEachPlayer(int damage) {
        return DealDamageEffect.toEachCreature(damage).andTo(Subjects.eachPlayer());
    }

    /** Puts the effect on the stack, dealt by {@code dealer} ({@code null}: a red spell); not yet resolved. */
    private void put(CardEffect effect, Permanent dealer) {
        Card card = dealer == null ? new Shock() : dealer.getCard();
        UUID sourceId = dealer == null ? null : dealer.getId();
        gd.stack.add(TestStackDoor.triggered(card, player1.getId(), "Mass damage", List.of(effect), null, sourceId));
    }

    @Nested
    @DisplayName("The creatures are found as the effect resolves")
    class SetFoundAsItResolves {

        @Test
        @DisplayName("A creature that arrived after the effect was put on the stack is dealt the damage")
        void arrivedLater() {
            Permanent early = addCreatureReady(player2, new GrizzlyBears());
            put(eachCreature(1), null);
            Permanent late = addCreatureReady(player2, new HillGiant());
            harness.passBothPriorities();

            assertThat(early.getMarkedDamage()).isEqualTo(1);
            assertThat(late.getMarkedDamage()).isEqualTo(1);
        }

        @Test
        @DisplayName("A creature that left before the effect resolved is dealt nothing")
        void leftBefore() {
            Permanent stays = addCreatureReady(player2, new GrizzlyBears());
            Permanent leaves = addCreatureReady(player2, new HillGiant());
            put(eachCreature(1), null);
            gd.playerBattlefields.get(player2.getId()).remove(leaves);
            harness.passBothPriorities();

            assertThat(stays.getMarkedDamage()).isEqualTo(1);
            assertThat(leaves.getMarkedDamage()).isZero();
        }
    }

    @Nested
    @DisplayName("Creatures and players are dealt the damage in one event")
    class OneEvent {

        @Test
        @DisplayName("A lifelink dealer's controller gains the total for creatures and players once")
        void lifelinkOnceForTheTotal() {
            Permanent sunstriker = addCreatureReady(player1, new AjanisSunstriker());
            Permanent pridemate = addCreatureReady(player1, new AjanisPridemate());
            addCreatureReady(player2, new GrizzlyBears());
            addCreatureReady(player2, new HillGiant());
            put(eachCreatureAndEachPlayer(1), sunstriker);
            harness.passBothPriorities();
            resolveAllTriggers();

            // Four creatures and two players dealt 1 each: 6 gained, 1 lost.
            harness.assertLife(player1, 25);
            harness.assertLife(player2, 19);
            assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("A \"whenever this creature is dealt damage\" ability triggers once")
        void dealtDamageTriggerOnce() {
            Permanent sliver = addCreatureReady(player2, new FungusSliver());
            put(eachCreatureAndEachPlayer(1), null);
            harness.passBothPriorities();
            resolveAllTriggers();

            assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("A \"whenever an opponent is dealt noncombat damage\" ability triggers once beside the creatures")
        void opponentDealtDamageTriggerOnce() {
            Permanent spitfire = addCreatureReady(player1, new ChandrasSpitfire());
            addCreatureReady(player2, new GrizzlyBears());
            put(eachCreatureAndEachPlayer(1), null);
            harness.passBothPriorities();
            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, spitfire)).isEqualTo(4);
        }

        @Test
        @DisplayName("Excess damage to several creatures is one \"one or more creatures\" trigger")
        void excessTriggerOnce() {
            addCreatureReady(player1, new MagmaticGalleon());
            addCreatureReady(player2, new GrizzlyBears());
            addCreatureReady(player2, new GrizzlyBears());
            put(eachCreature(3), null);
            harness.passBothPriorities();
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("The dealer's deathtouch makes 1 damage lethal to each creature")
    void deathtouchOverTheSet() {
        Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        put(eachCreature(1), nighthawk);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears, giant);
    }

    @Test
    @DisplayName("A face-down dealer is named by no card in the log")
    void faceDownDealer() {
        gd.gameLog.clear();
        Permanent hidden = addCreatureReady(player1, new HillGiant());
        hidden.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        put(eachCreatureAndEachPlayer(1), hidden);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(line -> line.contains("Hill Giant"));
    }
}
