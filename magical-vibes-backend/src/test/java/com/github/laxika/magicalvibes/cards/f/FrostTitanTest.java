package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FrostTitanTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Frost Titan has ON_BECOMES_TARGET_OF_OPPONENT_SPELL CounterUnlessPaysEffect(2)")
    void hasCounterTriggerEffect() {
        FrostTitan card = new FrostTitan();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL).getFirst())
                .isInstanceOf(CounterUnlessPaysEffect.class);
        assertThat(((CounterUnlessPaysEffect) card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL).getFirst()).amount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Frost Titan has ON_ENTER_BATTLEFIELD TapTargetPermanentEffect and SkipNextUntapOnTargetEffect")
    void hasETBEffects() {
        FrostTitan card = new FrostTitan();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD))
                .anyMatch(e -> e instanceof TapTargetPermanentEffect);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD))
                .anyMatch(e -> e instanceof SkipNextUntapOnTargetEffect);
    }

    @Test
    @DisplayName("Frost Titan has ON_ATTACK TapTargetPermanentEffect and SkipNextUntapOnTargetEffect")
    void hasAttackEffects() {
        FrostTitan card = new FrostTitan();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK))
                .anyMatch(e -> e instanceof TapTargetPermanentEffect);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK))
                .anyMatch(e -> e instanceof SkipNextUntapOnTargetEffect);
    }

    // ===== ETB trigger: tap target permanent + skip next untap =====

    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("ETB taps target permanent when Frost Titan enters the battlefield")
        void etbTapsTarget() {
            harness.addToBattlefield(player2, new GrizzlyBears());
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
            harness.addToBattlefield(player2, new GrizzlyBears());
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
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castFrostTitan(targetId);
            harness.passBothPriorities(); // resolve creature spell

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Frost Titan"));
        }
    }

    // ===== Attack trigger: tap target permanent + skip next untap =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Frost Titan queues attack trigger for target selection")
        void attackTriggerQueuesForTargetSelection() {
            Permanent frostTitan = addReadyFrostTitan(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());

            declareAttackers(List.of(0));

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        }

        @Test
        @DisplayName("Choosing target and resolving taps the target permanent")
        void attackTriggerTapsTarget() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent frostTitan = addReadyFrostTitan(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
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

            Permanent frostTitan = addReadyFrostTitan(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities(); // resolve attack trigger

            assertThat(bears.getSkipUntapCount()).isEqualTo(1);
        }
    }

    // ===== Becomes target of opponent spell: counter unless pays {2} =====

    @Nested
    @DisplayName("Becomes target of opponent spell trigger")
    class BecomesTargetTrigger {

        @Test
        @DisplayName("Triggers when opponent casts a spell targeting Frost Titan")
        void triggersOnOpponentSpellTargeting() {
            Permanent frostTitan = addReadyFrostTitan(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Shock()));
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
            Permanent frostTitan = addReadyFrostTitan(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1); // exact cost, no extra mana

            harness.castInstant(player2, 0, frostTitan.getId());

            // Resolve the counter trigger — opponent has no mana
            harness.passBothPriorities();

            // Shock should be countered
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Shock"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Opponent is prompted to pay when they have mana")
        void opponentPromptedWhenTheyHaveMana() {
            Permanent frostTitan = addReadyFrostTitan(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 3); // 1 to cast, 2 extra

            harness.castInstant(player2, 0, frostTitan.getId());
            harness.passBothPriorities(); // resolve counter trigger

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("Spell resolves when opponent pays {2}")
        void spellResolvesWhenOpponentPays() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent frostTitan = addReadyFrostTitan(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 3);

            harness.castInstant(player2, 0, frostTitan.getId());
            harness.passBothPriorities(); // resolve counter trigger

            harness.handleMayAbilityChosen(player2, true); // pay {2}

            // Shock should still be on the stack (not countered)
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Shock"));
        }

        @Test
        @DisplayName("Spell is countered when opponent declines to pay")
        void spellCounteredWhenOpponentDeclines() {
            Permanent frostTitan = addReadyFrostTitan(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 3);

            harness.castInstant(player2, 0, frostTitan.getId());
            harness.passBothPriorities(); // resolve counter trigger

            harness.handleMayAbilityChosen(player2, false); // decline to pay

            // Shock should be countered
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Shock"));
        }

        @Test
        @DisplayName("Does NOT trigger when controller casts a spell targeting Frost Titan")
        void doesNotTriggerOnControllerSpell() {
            Permanent frostTitan = addReadyFrostTitan(player1);

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, frostTitan.getId());

            // Only the Shock spell on the stack — no triggered ability
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        }
    }

    // ===== Helpers =====

    private void castFrostTitan(UUID targetPermanentId) {
        harness.setHand(player1, List.of(new FrostTitan()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0, 0, targetPermanentId);
    }

    private Permanent addReadyFrostTitan(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new FrostTitan());
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
