package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Each head primitive records what it did on the resolving entry's scratchpad, and a later step reads it in the order
 * written (CR 608.2c): "if you do" ({@link HappenedThisWay}), "that many" ({@link AmountThisWay}) and "those
 * permanents" / "that player" ({@code Subjects.thisWay} / {@code Subjects.playersThisWay}). Every case composes the
 * primitives the way a real card's text reads, without the card's own fused effect.
 */
@CardUsed({GrizzlyBears.class, HillGiant.class, Forest.class, GloriousAnthem.class, DarksteelMyr.class})
class StepResultsTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    private StackEntry resolve(Player controller, UUID targetId, UUID sourcePermanentId, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), controller.getId(), "Step results",
                List.of(effects), targetId, sourcePermanentId);
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    private static CardEffect ifDone(StepKind kind, CardEffect then) {
        return new ConditionalEffect(new HappenedThisWay(kind), then);
    }

    @Nested
    @DisplayName("Draw")
    class Draw {

        @Test
        @DisplayName("Baral, Chief of Compliance: \"you may draw a card. If you do, discard a card\" discards after a draw")
        void discardsAfterADraw() {
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null, new DrawCardEffect(1),
                    ifDone(StepKind.DRAW, new DiscardEffect(1, Subjects.you(), true)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DRAW).amount()).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        }

        @Test
        @DisplayName("A draw from an empty library draws nothing, so its \"if you do\" step does nothing")
        void nothingDrawnFromAnEmptyLibrary() {
            harness.setLibrary(player1, List.of());
            harness.setHand(player1, List.of(new GrizzlyBears()));

            StackEntry entry = resolve(player1, null, null, new DrawCardEffect(1),
                    ifDone(StepKind.DRAW, new DiscardEffect(1, Subjects.you(), true)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DRAW)).isEqualTo(StepOutcome.NONE);
            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        }

        @Test
        @DisplayName("The Royal Scions: \"draw four cards. When you do\" needs all four drawn (fewer don't count)")
        void fewerCardsThanWrittenDoNotCount() {
            harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

            resolve(player1, null, null, new DrawCardEffect(4),
                    new ConditionalEffect(HappenedThisWay.atLeast(StepKind.DRAW, 4), new GainLifeEffect(5)));

            assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("The Royal Scions: four cards drawn meet \"draw four cards. When you do\"")
        void theWrittenNumberCounts() {
            harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

            resolve(player1, null, null, new DrawCardEffect(4),
                    new ConditionalEffect(HappenedThisWay.atLeast(StepKind.DRAW, 4), new GainLifeEffect(5)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("Discard")
    class Discard {

        @Test
        @DisplayName("Mishra's Command: \"they draw a card for each card discarded this way\" counts the chosen discard")
        void drawsForEachCardDiscardedAfterTheChoice() {
            harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant()));
            harness.setLibrary(player1, List.of(new Forest(), new Forest()));

            StackEntry entry = resolve(player1, null, null, new DiscardEffect(1, Subjects.you(), false),
                    new DrawCardEffect(new AmountThisWay(StepKind.DISCARD)));
            harness.handleCardChosen(player1, 0);

            assertThat(entry.getResolutionResults().outcome(StepKind.DISCARD).amount()).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                    .containsExactlyInAnyOrder("Hill Giant", "Forest");
        }

        @Test
        @DisplayName("Discarding from an empty hand discards nothing, so nothing is drawn for it")
        void emptyHandDrawsNothing() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null, new DiscardEffect(2, Subjects.you(), true),
                    new DrawCardEffect(new AmountThisWay(StepKind.DISCARD)));

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mill")
    class Mill {

        @Test
        @DisplayName("Saprazzan Breaker: \"mill a card. If a land card was milled this way\" reads the milled card")
        void millRecordsTheMilledCard() {
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null, new MillEffect(1, Subjects.you()),
                    ifDone(StepKind.MILL, new GainLifeEffect(1)));

            StepOutcome milled = entry.getResolutionResults().outcome(StepKind.MILL);
            assertThat(milled.amount()).isEqualTo(1);
            assertThat(milled.playerIds()).containsExactly(player1.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }

        @Test
        @DisplayName("Milling more cards than the library holds mills as many as possible (CR 701.17b)")
        void millsAsManyAsPossible() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null, new MillEffect(3, Subjects.you()),
                    new GainLifeEffect(new AmountThisWay(StepKind.MILL)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("Exile")
    class Exile {

        @Test
        @DisplayName("Exile: \"exile target creature. You gain life\" records the exiled creature and its controller")
        void recordsTheExiledCreatureAndItsController() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            StackEntry entry = resolve(player1, bears.getId(), null, new ExileTargetPermanentEffect(),
                    new GainLifeEffect(2, Subjects.playersThisWay(StepKind.EXILE)));

            assertThat(entry.getResolutionResults().outcome(StepKind.EXILE).objectIds()).containsExactly(bears.getId());
            assertThat(gd.getLife(player2.getId())).isEqualTo(22);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Destroy")
    class Destroy {

        @Test
        @DisplayName("Break the Spell: \"destroy target enchantment. If ... was destroyed this way, draw a card\"")
        void drawsWhenTheTargetIsDestroyed() {
            Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, anthem.getId(), null, new DestroyTargetPermanentEffect(),
                    ifDone(StepKind.DESTROY, new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        }

        @Test
        @DisplayName("An indestructible target is not destroyed this way, so nothing is drawn")
        void indestructibleTargetIsNotDestroyed() {
            Permanent myr = harness.addToBattlefieldAndReturn(player1, new DarksteelMyr());
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, myr.getId(), null, new DestroyTargetPermanentEffect(),
                    ifDone(StepKind.DESTROY, new DrawCardEffect(1)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DESTROY).happened()).isFalse();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Sacrifice")
    class Sacrifice {

        @Test
        @DisplayName("Victimize: \"sacrifice a creature. If you do, ...\" reads the sacrifice")
        void readsTheSacrifice() {
            harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    ifDone(StepKind.SACRIFICE, new DrawCardEffect(1)));

            assertThat(entry.getResolutionResults().outcome(StepKind.SACRIFICE).playerIds())
                    .containsExactly(player1.getId());
            assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        }

        @Test
        @DisplayName("With no creature to sacrifice, the \"if you do\" step does nothing")
        void nothingToSacrifice() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null,
                    new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                    ifDone(StepKind.SACRIFICE, new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Return")
    class Return {

        @Test
        @DisplayName("Recoil: \"return target permanent to its owner's hand. Then that player discards a card\"")
        void thatPlayerIsTheOwner() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            StackEntry entry = resolve(player1, bears.getId(), null, ReturnToHandEffect.target(),
                    new DiscardEffect(1, Subjects.playersThisWay(StepKind.RETURN), true));

            assertThat(entry.getResolutionResults().outcome(StepKind.RETURN).playerIds())
                    .containsExactly(player2.getId());
            assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
        }
    }

    @Nested
    @DisplayName("Create token")
    class CreateToken {

        @Test
        @DisplayName("Turntimber Ranger: \"create a 2/2 green Wolf creature token. If you do, put a +1/+1 counter\"")
        void putsACounterAfterTheToken() {
            Permanent ranger = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            StackEntry entry = resolve(player1, null, ranger.getId(),
                    new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of()),
                    ifDone(StepKind.CREATE_TOKEN, new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));

            assertThat(entry.getResolutionResults().outcome(StepKind.CREATE_TOKEN).amount()).isEqualTo(1);
            assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Deal damage")
    class DealDamage {

        @Test
        @DisplayName("Syphon Soul: \"2 damage to each other player. You gain life equal to the damage dealt this way\"")
        void gainsTheDamageDealt() {
            resolve(player1, null, null, new DealDamageToPlayersEffect(2, Subjects.eachOpponent()),
                    new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE)));

            assertThat(gd.getLife(player2.getId())).isEqualTo(18);
            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("Provoke the Trolls: \"if a creature is dealt damage this way, it gets +5/+0\"")
        void boostsTheCreatureDealtDamage() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            StackEntry entry = resolve(player1, giant.getId(), null,
                    new DealDamageToAnyTargetEffect(new Fixed(1), false, false),
                    new BoostSelfEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE), 5, 0));

            assertThat(entry.getResolutionResults().outcome(StepKind.DEAL_DAMAGE).objectIds())
                    .containsExactly(giant.getId());
            assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(8);
        }

        @Test
        @DisplayName("Damage to a player is no creature dealt damage this way: the boost names nothing")
        void playerDamageNamesNoCreature() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

            resolve(player1, player2.getId(), null, new DealDamageToAnyTargetEffect(new Fixed(3), false, false),
                    new BoostSelfEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE), 5, 0));

            assertThat(gd.getLife(player2.getId())).isEqualTo(17);
            assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Gain and lose life")
    class Life {

        @Test
        @DisplayName("Gonti's Machinations: \"each opponent loses 3 life. You gain life equal to the life lost this way\"")
        void gainsTheLifeLost() {
            StackEntry entry = resolve(player1, null, null, new LoseLifeEffect(3, Subjects.eachOpponent()),
                    new GainLifeEffect(new AmountThisWay(StepKind.LOSE_LIFE)));

            assertThat(entry.getResolutionResults().outcome(StepKind.LOSE_LIFE).playerIds())
                    .containsExactly(player2.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(23);
            assertThat(entry.getResolutionResults().outcome(StepKind.GAIN_LIFE).amount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Gaining 0 life is no life gained this way")
        void zeroLifeGainedDidNotHappen() {
            harness.setLibrary(player1, List.of(new Forest()));

            resolve(player1, null, null, new GainLifeEffect(0),
                    ifDone(StepKind.GAIN_LIFE, new DrawCardEffect(1)));

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Counters")
    class Counters {

        @Test
        @DisplayName("Putting counters on target creature records how many and on which creature")
        void putCountersRecordsThem() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            StackEntry entry = resolve(player1, bears.getId(), null,
                    new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                    new GainLifeEffect(new AmountThisWay(StepKind.PUT_COUNTERS)));

            assertThat(entry.getResolutionResults().outcome(StepKind.PUT_COUNTERS).objectIds())
                    .containsExactly(bears.getId());
            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("Living Artifact: \"remove a vitality counter from it. If you do, you gain 1 life\"")
        void gainsOnlyWhenACounterCameOff() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            bears.setCounterCount(CounterType.VITALITY, 1);
            CardEffect remove = new RemoveCounterFromTargetPermanentEffect(CounterType.VITALITY, null);

            resolve(player1, bears.getId(), null, remove, ifDone(StepKind.REMOVE_COUNTERS, new GainLifeEffect(1)));
            resolve(player1, bears.getId(), null, remove, ifDone(StepKind.REMOVE_COUNTERS, new GainLifeEffect(1)));

            assertThat(bears.getCounterCount(CounterType.VITALITY)).isZero();
            assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("Reading rules")
    class Reading {

        @Test
        @DisplayName("\"If you don't\" is the negation: it applies when the earlier step did nothing")
        void ifYouDont() {
            harness.setLibrary(player1, List.of());

            resolve(player1, null, null, new DrawCardEffect(1),
                    new ConditionalEffect(new NotCondition(HappenedThisWay.drew()), new GainLifeEffect(2)));

            assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        }

        @Test
        @DisplayName("A later step of the same kind replaces the earlier step's result, even when it did nothing")
        void aLaterStepReplacesTheEarlierResult() {
            harness.setLibrary(player1, List.of(new Forest()));

            StackEntry entry = resolve(player1, null, null, new DrawCardEffect(1), new DrawCardEffect(1),
                    ifDone(StepKind.DRAW, new GainLifeEffect(5)));

            assertThat(entry.getResolutionResults().outcome(StepKind.DRAW)).isEqualTo(StepOutcome.NONE);
            assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("A subject that duplicates an existing step-result subject is refused")
        void duplicateSubjectsAreRefused() {
            assertThatThrownBy(() -> Subjects.thisWay(StepKind.CREATE_TOKEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("createdThisResolution");
            assertThatThrownBy(() -> Subjects.thisWay(StepKind.DESTROY))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Subjects.playersThisWay(StepKind.DEAL_DAMAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("playersDealtDamageThisResolution");
        }
    }
}
