package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

class SunTitanTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Sun Titan has MayEffect wrapping ReturnCardFromGraveyardEffect on ETB and attack")
    void hasCorrectEffects() {
        SunTitan card = new SunTitan();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect etbMay = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etbMay.wrapped()).isInstanceOf(ReturnCardFromGraveyardEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(MayEffect.class);
    }

    // ===== ETB trigger =====

    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("Casting Sun Titan triggers may ability prompt")
        void etbTriggersMayPrompt() {
            harness.setGraveyard(player1, List.of(new GrizzlyBears()));
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may and resolving returns permanent with MV ≤ 3 to battlefield")
        void returnsPermanentWithLowManaValue() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → graveyard choice

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
            harness.handleGraveyardCardChosen(player1, 0);

            // Grizzly Bears should now be on the battlefield
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Can return a land (permanent with MV 0) to battlefield")
        void returnsLandToBattlefield() {
            Card plains = new Plains();
            harness.setGraveyard(player1, List.of(plains));
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → graveyard choice

            harness.handleGraveyardCardChosen(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Plains"));
        }

        @Test
        @DisplayName("Cannot return non-permanent card (instant) from graveyard")
        void cannotReturnNonPermanent() {
            harness.setGraveyard(player1, List.of(new HolyDay()));
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

            // No valid targets — should skip graveyard choice
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Cannot return permanent with MV > 3 from graveyard")
        void cannotReturnHighManaValuePermanent() {
            harness.setGraveyard(player1, List.of(new StoneGolem())); // MV 5
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

            // No valid targets — should skip graveyard choice
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Declining may ability does not return anything")
        void decliningMaySkipsReturn() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
            // Grizzly Bears still in graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("ETB resolves with no effect if graveyard is empty")
        void noEffectWithEmptyGraveyard() {
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        }

        @Test
        @DisplayName("Only permanent cards with MV ≤ 3 are offered — filters out high MV and non-permanents")
        void filtersCorrectly() {
            // Mix of valid and invalid targets
            Card bears = new GrizzlyBears(); // creature, MV 2 — valid
            Card holyDay = new HolyDay(); // instant — invalid (non-permanent)
            Card stoneGolem = new StoneGolem(); // creature, MV 5 — invalid (MV > 3)
            Card plains = new Plains(); // land, MV 0 — valid
            harness.setGraveyard(player1, List.of(bears, holyDay, stoneGolem, plains));
            castSunTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → graveyard choice

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

            // Choose the first valid card (Grizzly Bears)
            harness.handleGraveyardCardChosen(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }
    }

    // ===== Attack trigger =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Sun Titan triggers may ability prompt")
        void attackTriggersMayPrompt() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            addReadySunTitan(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting attack may and picking a card puts permanent on battlefield")
        void attackReturnsCard() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            addReadySunTitan(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → graveyard choice

            harness.handleGraveyardCardChosen(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Declining attack may ability skips graveyard return")
        void decliningAttackMaySkipsReturn() {
            Card bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));
            addReadySunTitan(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    // ===== Helpers =====

    private void castSunTitan() {
        harness.setHand(player1, List.of(new SunTitan()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadySunTitan(Player player) {
        Permanent perm = new Permanent(new SunTitan());
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
