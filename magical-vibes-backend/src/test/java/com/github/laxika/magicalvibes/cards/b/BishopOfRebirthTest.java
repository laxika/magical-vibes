package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StoneGolem;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BishopOfRebirthTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Bishop of Rebirth has MayEffect wrapping ReturnCardFromGraveyardEffect on attack only")
    void hasCorrectEffects() {
        BishopOfRebirth card = new BishopOfRebirth();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect attackMay = (MayEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(attackMay.wrapped()).isInstanceOf(ReturnCardFromGraveyardEffect.class);

        // Should NOT have an ETB trigger (unlike Sun Titan)
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).isEmpty();
    }

    // ===== Attack trigger =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Bishop of Rebirth triggers may ability prompt")
        void attackTriggersMayPrompt() {
            harness.setGraveyard(player1, List.of(new GrizzlyBears()));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may and picking a creature returns it to battlefield")
        void returnsCreatureWithLowManaValue() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
            harness.handleGraveyardCardChosen(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot return non-creature card (instant) from graveyard")
        void cannotReturnNonCreature() {
            harness.setGraveyard(player1, List.of(new HolyDay()));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            // No valid targets — should skip graveyard choice
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Cannot return non-creature permanent (land) from graveyard")
        void cannotReturnNonCreaturePermanent() {
            harness.setGraveyard(player1, List.of(new Plains()));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            // Plains is a permanent but not a creature — should skip graveyard choice
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Cannot return creature with MV > 3 from graveyard")
        void cannotReturnHighManaValueCreature() {
            harness.setGraveyard(player1, List.of(new StoneGolem())); // MV 5
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            // No valid targets — should skip graveyard choice
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Declining may ability does not return anything")
        void decliningMaySkipsReturn() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Attack resolves with no effect if graveyard is empty")
        void noEffectWithEmptyGraveyard() {
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Only creature cards with MV ≤ 3 are valid — filters out high MV, non-creatures, and instants")
        void filtersCorrectly() {
            Card bears = new GrizzlyBears(); // creature, MV 2 — valid
            Card holyDay = new HolyDay(); // instant — invalid (non-creature)
            Card stoneGolem = new StoneGolem(); // creature, MV 5 — invalid (MV > 3)
            Card plains = new Plains(); // land, MV 0 — invalid (non-creature)
            harness.setGraveyard(player1, List.of(bears, holyDay, stoneGolem, plains));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

            // Only Grizzly Bears should be a valid choice
            harness.handleGraveyardCardChosen(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }
    }

    // ===== Helpers =====

    private Permanent addReadyBishop(Player player) {
        Permanent perm = new Permanent(new BishopOfRebirth());
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
