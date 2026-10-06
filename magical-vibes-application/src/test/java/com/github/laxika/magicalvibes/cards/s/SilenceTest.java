package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Silence.class, RuneclawBear.class, LightningBolt.class, Forest.class, ProdigalPyromancer.class, AncestralVision.class})
class SilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Silence puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new Silence(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Silence.class);
    }

    @Nested
    @DisplayName("Opponents can't cast spells this turn")
    @CardUsed({Silence.class, RuneclawBear.class, LightningBolt.class, Forest.class, ProdigalPyromancer.class})
    class OpponentsCantCast {

        @Test
        @DisplayName("Resolving Silence silences the opponent")
        void resolvingSilencesOpponent() {
            harness.castFromHand(player1, new Silence(), "{W}");
            harness.passBothPriorities();

            assertThat(gd.playersSilencedThisTurn).contains(player2.getId());
            assertThat(gd.playersSilencedThisTurn).doesNotContain(player1.getId());
        }

        @Test
        @DisplayName("Silenced opponent cannot cast creature spells")
        void silencedOpponentCannotCastCreature() {
            harness.castFromHand(player1, new Silence(), "{W}");
            harness.passBothPriorities();

            // Switch to opponent's turn
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new RuneclawBear()));
            harness.addMana(player2, ManaColor.GREEN, 2);

            // Opponent should not be able to cast
            assertThatThrownBy(() -> harness.castCreature(player2, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Silenced opponent cannot cast instant spells")
        void silencedOpponentCannotCastInstant() {
            harness.castFromHand(player1, new Silence(), "{W}");
            harness.passBothPriorities();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            // Opponent should not be able to cast instants either
            assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Silence caster can still cast spells")
        void casterCanStillCast() {
            harness.castFromHand(player1, new Silence(), "{W}");
            harness.passBothPriorities();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player1, List.of(new RuneclawBear()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            // Caster should still be able to cast
            harness.castCreature(player1, 0);
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Silenced opponent can still play lands")
        void silencedOpponentCanStillPlayLands() {
            harness.castFromHand(player1, new Silence(), "{W}");
            harness.passBothPriorities();

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Forest()));

            GameActionAvailabilityService gbs = harness.getGameActionAvailabilityService();
            List<Integer> playable = gbs.getPlayableCardIndices(gd, player2.getId());

            // Land should still be playable
            assertThat(playable).contains(0);
        }

        @Test
        @DisplayName("Silenced opponent can still activate abilities")
        void silencedOpponentCanStillActivateAbilities() {
            harness.castFromHand(player1, new Silence(), "{W}");
            harness.passBothPriorities();

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Give opponent a Prodigal Pyromancer (tap: deal 1 damage to any target)
            addCreatureReady(player2, new ProdigalPyromancer());

            // Opponent should be able to activate the ability even while silenced
            harness.passPriority(player1);
            harness.activateAbility(player2, 0, null, player1.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Prodigal Pyromancer");
        }

        @Test
        @DisplayName("Spells already on the stack are not affected by Silence")
        void spellsAlreadyOnStackNotAffected() {
            // Opponent casts a creature spell
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            RuneclawBear bears = new RuneclawBear();
            harness.setHand(player2, List.of(bears));
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.castCreature(player2, 0);

            // Player1 responds with Silence
            harness.castFromHand(player1, new Silence(), "{W}");

            // Silence resolves first (top of stack)
            harness.passBothPriorities();

            // Runeclaw Bear should still be on the stack and resolve normally
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Runeclaw Bear");

            harness.passBothPriorities();

            // Bears should enter the battlefield
            harness.assertOnBattlefield(player2, "Runeclaw Bear");
        }

        @Test
        @DisplayName("Silence goes to graveyard after resolution")
        void goesToGraveyardAfterResolution() {
            harness.castFromHand(player1, new Silence(), "{W}");
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Silence");
        }
    }

    @Test
    @DisplayName("Silence restriction is cleared at end of turn")
    void restrictionClearedAtEndOfTurn() {
        harness.castFromHand(player1, new Silence(), "{W}");
        harness.passBothPriorities();

        TurnCleanupService svc = GameTestEngineContext.get().getBean(TurnCleanupService.class);
        svc.resetEndOfTurnModifiers(gd);

        assertThat(gd.playersSilencedThisTurn).isEmpty();
    }

    @Test
    @DisplayName("Opponent can respond before Silence resolves")
    void opponentCanRespondBeforeResolution() {
        harness.castFromHand(player1, new Silence(), "{W}");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playersSilencedThisTurn).doesNotContain(player2.getId());

        harness.passBothPriorities();
        assertThat(gd.playersSilencedThisTurn).contains(player2.getId());
    }

    @Test
    @DisplayName("Opponent can cast again on the next turn")
    void opponentCanCastOnNextTurn() {
        harness.castFromHand(player1, new Silence(), "{W}");
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new RuneclawBear(), "{1}{G}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }
    @Test
    @CardUsed({Silence.class, AncestralVision.class})
    @DisplayName("Silenced opponent cannot suspend a card")
    void silencedOpponentCannotSuspend() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Silence(), "{W}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new AncestralVision()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
