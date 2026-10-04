package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostTitan.class, RuneclawBear.class, LightningBolt.class, ProdigalPyromancer.class})
class FrostTitanTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({FrostTitan.class, RuneclawBear.class})
    class ETBTrigger {

        @Test
        @DisplayName("ETB taps target permanent when Frost Titan enters the battlefield")
        void etbTapsTarget() {
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();
            UUID targetId = bears.getId();

            castFrostTitan(targetId);
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("ETB sets skipUntapCount on target permanent")
        void etbSetsSkipUntap() {
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            UUID targetId = bears.getId();

            castFrostTitan(targetId);
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Frost Titan enters the battlefield after casting")
        void frostTitanEntersBattlefield() {
            harness.addToBattlefield(player2, new RuneclawBear());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castFrostTitan(targetId);
            harness.passBothPriorities(); // resolve creature spell

            harness.assertOnBattlefield(player1, "Frost Titan");
        }
    }

    @Nested
    @DisplayName("Attack trigger")
    @CardUsed({FrostTitan.class, RuneclawBear.class})
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Frost Titan queues attack trigger for target selection")
        void attackTriggerQueuesForTargetSelection() {
            addCreatureReady(player1, new FrostTitan());
            harness.addToBattlefield(player2, new RuneclawBear());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Choosing target and resolving taps the target permanent")
        void attackTriggerTapsTarget() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addCreatureReady(player1, new FrostTitan());
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Choosing target and resolving sets skipUntapCount on target")
        void attackTriggerSetsSkipUntap() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addCreatureReady(player1, new FrostTitan());
            harness.addToBattlefield(player2, new RuneclawBear());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Becomes target of opponent spell trigger")
    @CardUsed({FrostTitan.class, LightningBolt.class})
    class BecomesTargetTrigger {

        @Test
        @DisplayName("Triggers when opponent casts a spell targeting Frost Titan")
        void triggersOnOpponentSpellTargeting() {
            Permanent frostTitan = addCreatureReady(player1, new FrostTitan());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castInstant(player2, 0, frostTitan.getId());

            // Spell + counter trigger on stack
            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Frost Titan");
        }

        @Test
        @DisplayName("Counters opponent's spell when opponent has no mana to pay {2}")
        void countersWhenOpponentCannotPay() {
            Permanent frostTitan = addCreatureReady(player1, new FrostTitan());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1); // exact cost, no extra mana

            harness.castInstant(player2, 0, frostTitan.getId());

            // Resolve the counter trigger — opponent has no mana
            harness.passBothPriorities();

            // Lightning Bolt should be countered
            harness.assertInGraveyard(player2, "Lightning Bolt");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Opponent is prompted to pay when they have mana")
        void opponentPromptedWhenTheyHaveMana() {
            Permanent frostTitan = addCreatureReady(player1, new FrostTitan());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 3); // 1 to cast, 2 extra

            harness.castInstant(player2, 0, frostTitan.getId());
            harness.passBothPriorities(); // resolve counter trigger

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("Spell resolves when opponent pays {2}")
        void spellResolvesWhenOpponentPays() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent frostTitan = addCreatureReady(player1, new FrostTitan());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 3);

            harness.castInstant(player2, 0, frostTitan.getId());
            harness.passBothPriorities(); // resolve counter trigger

            harness.handleMayAbilityChosen(player2, true); // pay {2}

            // Lightning Bolt should still be on the stack (not countered)
            harness.assertNotInGraveyard(player2, "Lightning Bolt");
        }

        @Test
        @DisplayName("Spell is countered when opponent declines to pay")
        void spellCounteredWhenOpponentDeclines() {
            Permanent frostTitan = addCreatureReady(player1, new FrostTitan());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 3);

            harness.castInstant(player2, 0, frostTitan.getId());
            harness.passBothPriorities(); // resolve counter trigger

            harness.handleMayAbilityChosen(player2, false); // decline to pay

            // Lightning Bolt should be countered
            harness.assertInGraveyard(player2, "Lightning Bolt");
        }

        @Test
        @DisplayName("Does NOT trigger when controller casts a spell targeting Frost Titan")
        void doesNotTriggerOnControllerSpell() {
            Permanent frostTitan = addCreatureReady(player1, new FrostTitan());

            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, frostTitan.getId());

            // Only the Lightning Bolt spell on the stack — no triggered ability
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
        }
    }

    @Test
    void alreadyTappedTargetSkipsOnlyItsNextUntapStep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.tap();

        castFrostTitan(bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void canTargetItselfOnEntering() {
        harness.setHand(player1, List.of(new FrostTitan()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent titan = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, titan.getId());
        harness.passBothPriorities();

        assertThat(titan.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(titan.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(titan.isTapped()).isFalse();
    }

    @Test
    void countersOpponentsActivatedAbilityWithoutRemovingItsSource() {
        Permanent titan = addCreatureReady(player1, new FrostTitan());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, titan.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(titan.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        assertThat(pyromancer.isTapped()).isTrue();
        harness.assertNotInGraveyard(player2, "Prodigal Pyromancer");
    }

    private void castFrostTitan(UUID targetId) {
        harness.setHand(player1, List.of(new FrostTitan()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0, 0, targetId);
    }

}
