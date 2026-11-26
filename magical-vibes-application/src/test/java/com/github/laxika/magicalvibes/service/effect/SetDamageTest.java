package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.f.FungusSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.Prevention;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Damage to a set of permanents ({@link DealDamageEffect#toEach}: each creature target player controls, each creature
 * the targeted player or planeswalker's controller controls, each creature defending player controls) and to each
 * target ({@link DealDamageEffect#toEachTarget}): the set is found as the effect is applied (CR 608.2h), the whole set is dealt its
 * damage in one damage event (one lifelink gain for the total, CR 702.15b; one "is dealt damage" trigger per
 * permanent), the dealer's deathtouch applies to each permanent, and a face-down dealer is named by no card (CR 708.2).
 */
@CardUsed({AjanisPridemate.class, FungusSliver.class, GrizzlyBears.class, HillGiant.class, ProdigalPyromancer.class,
        Shock.class, VampireNighthawk.class})
class SetDamageTest extends BaseCardTest {

    private static final PermanentIsCreaturePredicate CREATURE = new PermanentIsCreaturePredicate();

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    /** The four set shapes, each dealing 1 damage to player 2's creatures. */
    private enum Shape {
        EACH_MATCHING, TARGET_PLAYER_CONTROLS, DEFENDING_PLAYER, EACH_TARGET
    }

    private CardEffect effect(Shape shape, int damage) {
        return switch (shape) {
            case EACH_MATCHING -> DealDamageEffect.toEach(Subjects.controlledBy(Subjects.targetPlayer(), CREATURE),
                    damage);
            case TARGET_PLAYER_CONTROLS -> DealDamageEffect.toEachControlledByTargetedPlayer(damage, CREATURE);
            case DEFENDING_PLAYER -> DealDamageEffect.toEach(Subjects.controlledBy(Subjects.defendingPlayer(), CREATURE),
                    damage);
            case EACH_TARGET -> DealDamageEffect.toEachTarget(new Fixed(damage));
        };
    }

    /** Puts the shape's entry on the stack, dealt by {@code dealer} ({@code null}: a red spell); not yet resolved. */
    private StackEntry put(Shape shape, int damage, Permanent dealer, List<Permanent> eachTargets) {
        Card card = dealer == null ? new Shock() : dealer.getCard();
        UUID sourceId = dealer == null ? null : dealer.getId();
        StackEntry entry = shape == Shape.EACH_TARGET
                ? TestStackDoor.triggered(card, player1.getId(), "Set damage", List.of(effect(shape, damage)), sourceId,
                eachTargets.stream().map(Permanent::getId).toList())
                : TestStackDoor.triggered(card, player1.getId(), "Set damage", List.of(effect(shape, damage)),
                player2.getId(), sourceId);
        if (shape == Shape.DEFENDING_PLAYER) {
            entry.setAttackedTargetId(player2.getId());
            entry.setDefendingPlayerId(player2.getId());
        }
        gd.stack.add(entry);
        return entry;
    }

    private void forEachShape(Consumer<Shape> check) {
        for (Shape shape : Shape.values()) {
            check.accept(shape);
        }
    }

    @Nested
    @DisplayName("The set is found as the effect resolves")
    class SetFoundAsItResolves {

        @Test
        @DisplayName("A creature that arrived after the spell was put on the stack is dealt the damage")
        void arrivedLater() {
            forEachShape(shape -> {
                gd.playerBattlefields.get(player2.getId()).clear();
                Permanent early = addCreatureReady(player2, new GrizzlyBears());
                List<Permanent> targets = shape == Shape.EACH_TARGET ? List.of(early) : List.of();
                put(shape, 1, null, targets);
                if (shape == Shape.EACH_TARGET) {
                    // Each target is the targets chosen: a later creature is no target, so it is dealt nothing.
                    Permanent late = addCreatureReady(player2, new HillGiant());
                    harness.passBothPriorities();
                    assertThat(early.getMarkedDamage()).as(shape.name()).isEqualTo(1);
                    assertThat(late.getMarkedDamage()).as(shape.name()).isZero();
                    return;
                }
                Permanent late = addCreatureReady(player2, new HillGiant());
                harness.passBothPriorities();
                assertThat(early.getMarkedDamage()).as(shape.name()).isEqualTo(1);
                assertThat(late.getMarkedDamage()).as(shape.name()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("A creature that left before the effect resolved is dealt nothing")
        void leftBefore() {
            forEachShape(shape -> {
                gd.playerBattlefields.get(player2.getId()).clear();
                Permanent stays = addCreatureReady(player2, new GrizzlyBears());
                Permanent leaves = addCreatureReady(player2, new HillGiant());
                put(shape, 1, null, List.of(stays, leaves));
                gd.playerBattlefields.get(player2.getId()).remove(leaves);
                harness.passBothPriorities();
                assertThat(stays.getMarkedDamage()).as(shape.name()).isEqualTo(1);
                assertThat(leaves.getMarkedDamage()).as(shape.name()).isZero();
            });
        }
    }

    @Nested
    @DisplayName("One damage event for the whole set")
    class OneEvent {

        @Test
        @DisplayName("A lifelink dealer's controller gains the total once")
        void lifelinkOnceForTheTotal() {
            forEachShape(shape -> {
                gd.playerBattlefields.get(player1.getId()).clear();
                gd.playerBattlefields.get(player2.getId()).clear();
                gd.playerLifeTotals.put(player1.getId(), 20);
                Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());
                Permanent pridemate = addCreatureReady(player1, new AjanisPridemate());
                Permanent bears = addCreatureReady(player2, new GrizzlyBears());
                Permanent giant = addCreatureReady(player2, new HillGiant());
                put(shape, 1, nighthawk, List.of(bears, giant));
                harness.passBothPriorities();
                harness.passBothPriorities();

                harness.assertLife(player1, 22);
                assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).as(shape.name()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("A \"whenever this creature is dealt damage\" ability triggers once")
        void dealtDamageTriggerOnce() {
            forEachShape(shape -> {
                gd.playerBattlefields.get(player2.getId()).clear();
                Permanent sliver = addCreatureReady(player2, new FungusSliver());
                put(shape, 1, null, List.of(sliver));
                harness.passBothPriorities();
                harness.passBothPriorities();
                assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).as(shape.name()).isEqualTo(1);
            });
        }
    }

    @Test
    @DisplayName("The dealer's deathtouch makes 1 damage lethal to each permanent of the set")
    void deathtouchOverASet() {
        forEachShape(shape -> {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).clear();
            Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());
            put(shape, 1, nighthawk, List.of(bears, giant));
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player2.getId())).as(shape.name()).doesNotContain(bears, giant);
        });
    }

    @Test
    @DisplayName("A face-down dealer is named by no card in the log")
    void faceDownDealer() {
        forEachShape(shape -> {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).clear();
            gd.gameLog.clear();
            Permanent hidden = addCreatureReady(player1, new HillGiant());
            hidden.setFaceDown(2, 2, Set.of(CardType.CREATURE));
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            put(shape, 1, hidden, List.of(bears));
            harness.passBothPriorities();
            assertThat(bears.getMarkedDamage()).as(shape.name()).isEqualTo(1);
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).as(shape.name())
                    .noneMatch(line -> line.contains("Hill Giant"));
        });
    }
    /**
     * A permanent dealing damage as the source of its ability has its current colour (CR 608.2h): a red permanent made
     * blue deals blue damage, which "prevent all damage from red sources" doesn't prevent.
     */
    @Nested
    @DisplayName("A recoloured permanent deals damage of its current colour")
    class RecolouredDealer {

        @Test
        @DisplayName("Its damage to each creature of a set isn't prevented as red")
        void setDamage() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent dealer = blueRedCreature();
            resolveFrom(dealer, null,
                    DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachOpponent(), CREATURE), 1));
            assertThat(bears.getMarkedDamage()).isEqualTo(1);
        }

        @Test
        @DisplayName("Its damage to a target creature isn't prevented as red")
        void targetDamage() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            Permanent dealer = blueRedCreature();
            resolveFrom(dealer, bears.getId(), DealDamageEffect.toTargetCreature(1));
            assertThat(bears.getMarkedDamage()).isEqualTo(1);
        }

        private Permanent blueRedCreature() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            Permanent dealer = addCreatureReady(player1, new ProdigalPyromancer());
            resolveFrom(dealer, null, new GrantColorUntilEndOfTurnEffect(CardColor.BLUE, Subjects.source()));
            return dealer;
        }

        private void resolveFrom(Permanent dealer, UUID targetId, CardEffect effect) {
            gd.stack.add(TestStackDoor.triggered(dealer.getCard(), player1.getId(), "Recoloured", List.of(effect),
                    targetId, dealer.getId()));
            harness.passBothPriorities();
        }
    }
}
