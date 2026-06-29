package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerritorialHammerskullTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_ATTACK TapTargetPermanentEffect")
    void hasAttackTapEffect() {
        TerritorialHammerskull card = new TerritorialHammerskull();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(TapTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Has a target filter restricting to opponent creatures")
    void hasTargetFilter() {
        TerritorialHammerskull card = new TerritorialHammerskull();

        assertThat(card.getSpellTargets()).hasSize(1);
    }

    // ===== Attack trigger =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking queues attack trigger for target selection")
        void attackTriggerQueuesForTargetSelection() {
            Permanent hammerskull = addReadyHammerskull(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Resolving attack trigger taps target creature")
        void attackTriggerTapsTarget() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent hammerskull = addReadyHammerskull(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(bears.isTapped()).isFalse();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Does not tap own creatures — only opponent's creatures are valid targets")
        void cannotTargetOwnCreatures() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent hammerskull = addReadyHammerskull(player1);
            // Add a second creature for player1 (own creature)
            harness.addToBattlefield(player1, new GrizzlyBears());
            // Add opponent creature
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent ownBears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).getFirst();

            declareAttackers(List.of(0));
            // Choose the opponent's creature
            harness.handlePermanentChosen(player1, opponentBears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            // Opponent creature is tapped, own creature is not
            assertThat(opponentBears.isTapped()).isTrue();
            assertThat(ownBears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Already tapped creature can still be targeted")
        void canTargetAlreadyTappedCreature() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent hammerskull = addReadyHammerskull(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            bears.tap();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(bears.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Attack trigger puts triggered ability on the stack")
        void attackTriggerPutsOnStack() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent hammerskull = addReadyHammerskull(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());

            assertThat(gd.stack).isNotEmpty();
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Territorial Hammerskull");
        }
    }

    // ===== Helpers =====

    private Permanent addReadyHammerskull(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new TerritorialHammerskull());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
