package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SilenceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Silence has correct effect")
    void hasCorrectEffect() {
        Silence card = new Silence();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(OpponentsCantCastSpellsThisTurnEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Silence puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Silence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Silence");
    }

    // ===== Resolution and restriction =====

    @Nested
    @DisplayName("Opponents can't cast spells this turn")
    class OpponentsCantCast {

        @Test
        @DisplayName("Resolving Silence silences the opponent")
        void resolvingSilencesOpponent() {
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castAndResolveInstant(player1, 0);

            assertThat(gd.playersSilencedThisTurn).contains(player2.getId());
            assertThat(gd.playersSilencedThisTurn).doesNotContain(player1.getId());
        }

        @Test
        @DisplayName("Silenced opponent cannot cast creature spells")
        void silencedOpponentCannotCastCreature() {
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castAndResolveInstant(player1, 0);

            // Switch to opponent's turn
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new GrizzlyBears()));
            harness.addMana(player2, ManaColor.GREEN, 2);

            // Opponent should not be able to cast
            assertThatThrownBy(() -> harness.castCreature(player2, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Silenced opponent cannot cast instant spells")
        void silencedOpponentCannotCastInstant() {
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castAndResolveInstant(player1, 0);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);

            // Opponent should not be able to cast instants either
            assertThatThrownBy(() -> harness.castInstant(player2, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Silence caster can still cast spells")
        void casterCanStillCast() {
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castAndResolveInstant(player1, 0);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 2);

            // Caster should still be able to cast
            harness.castCreature(player1, 0);
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Silenced opponent can still play lands")
        void silencedOpponentCanStillPlayLands() {
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castAndResolveInstant(player1, 0);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Forest()));

            GameBroadcastService gbs = harness.getGameBroadcastService();
            List<Integer> playable = gbs.getPlayableCardIndices(gd, player2.getId());

            // Land should still be playable
            assertThat(playable).contains(0);
        }

        @Test
        @DisplayName("Silenced opponent can still activate abilities")
        void silencedOpponentCanStillActivateAbilities() {
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castAndResolveInstant(player1, 0);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            // Give opponent a Prodigal Pyromancer (tap: deal 1 damage to any target)
            harness.addToBattlefield(player2, new ProdigalPyromancer());
            Permanent pyroPerm = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Prodigal Pyromancer"))
                    .findFirst().orElseThrow();
            pyroPerm.setSummoningSick(false);

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

            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player2, List.of(bears));
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.castCreature(player2, 0);

            // Player1 responds with Silence
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castInstant(player1, 0);

            // Silence resolves first (top of stack)
            harness.passBothPriorities();

            // Grizzly Bears should still be on the stack and resolve normally
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");

            harness.passBothPriorities();

            // Bears should enter the battlefield
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Silence goes to graveyard after resolution")
        void goesToGraveyardAfterResolution() {
            harness.setHand(player1, List.of(new Silence()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castAndResolveInstant(player1, 0);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Silence"));
        }
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Silence restriction is cleared at end of turn")
    void restrictionClearedAtEndOfTurn() {
        gd.playersSilencedThisTurn.add(player2.getId());

        TurnCleanupService svc = new TurnCleanupService(null);
        svc.resetEndOfTurnModifiers(gd);

        assertThat(gd.playersSilencedThisTurn).isEmpty();
    }
}
