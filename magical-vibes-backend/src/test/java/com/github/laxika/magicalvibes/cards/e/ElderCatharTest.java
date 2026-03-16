package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChampionOfTheParish;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSubtypeReplacementEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElderCatharTest extends BaseCardTest {

    /**
     * Sets up combat where Elder Cathar (player1) attacks and is blocked by a 3/3 creature (player2).
     * Elder Cathar will die from combat damage.
     */
    private void setupCombatWhereElderCatharDies() {
        Permanent catharPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elder Cathar"))
                .findFirst().orElseThrow();
        catharPerm.setSummoningSick(false);
        catharPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_DEATH TargetSubtypeReplacementEffect wrapping PutPlusOnePlusOneCounterOnTargetCreatureEffect")
    void hasCorrectEffects() {
        ElderCathar card = new ElderCathar();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(TargetSubtypeReplacementEffect.class);

        TargetSubtypeReplacementEffect wrapper =
                (TargetSubtypeReplacementEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(wrapper.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(wrapper.baseEffect()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        assertThat(((PutPlusOnePlusOneCounterOnTargetCreatureEffect) wrapper.baseEffect()).count()).isEqualTo(1);
        assertThat(wrapper.upgradedEffect()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        assertThat(((PutPlusOnePlusOneCounterOnTargetCreatureEffect) wrapper.upgradedEffect()).count()).isEqualTo(2);
    }

    // ===== Death trigger =====

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Elder Cathar dies, controller is prompted to choose a target creature they control")
        void deathTriggerPromptsForTarget() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new GrizzlyBears());
            setupCombatWhereElderCatharDies();

            harness.passBothPriorities(); // Combat damage — Elder Cathar dies

            GameData gd = harness.getGameData();

            // Elder Cathar should be dead
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Elder Cathar"));

            // Controller should be prompted to choose a target creature
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Puts 1 +1/+1 counter on a non-Human creature")
        void putsOneCounterOnNonHuman() {
            harness.addToBattlefield(player1, new ElderCathar());
            GrizzlyBears bear = new GrizzlyBears();
            harness.addToBattlefield(player1, bear);

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Combat damage — Elder Cathar dies

            // Choose the non-Human Grizzly Bears
            harness.handlePermanentChosen(player1, bearId);

            // Triggered ability should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

            // Resolve the triggered ability
            harness.passBothPriorities();

            // Grizzly Bears should have 1 +1/+1 counter
            Permanent bearsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(bearId))
                    .findFirst().orElseThrow();
            assertThat(bearsPerm.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(bearsPerm.getEffectivePower()).isEqualTo(3);
            assertThat(bearsPerm.getEffectiveToughness()).isEqualTo(3);
        }

        @Test
        @DisplayName("Puts 2 +1/+1 counters on a Human creature instead of 1")
        void putsTwoCountersOnHuman() {
            harness.addToBattlefield(player1, new ElderCathar());
            // Champion of the Parish is a Human
            harness.addToBattlefield(player1, new ChampionOfTheParish());

            UUID championId = harness.getPermanentId(player1, "Champion of the Parish");

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Combat damage — Elder Cathar dies

            // Choose the Human Champion of the Parish
            harness.handlePermanentChosen(player1, championId);

            // Resolve the triggered ability
            harness.passBothPriorities();

            // Champion of the Parish should have 2 +1/+1 counters (the "instead" clause)
            Permanent championPerm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(championId))
                    .findFirst().orElseThrow();
            assertThat(championPerm.getPlusOnePlusOneCounters()).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target opponent's creature (targets creature you control)")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player1, new ElderCathar());
            // Only opponent has a creature (besides the blocker)
            // No creatures on player1's side other than the dying Elder Cathar

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Combat damage — Elder Cathar dies

            GameData gd = harness.getGameData();

            // No valid targets (player1 has no creatures, can't target opponent's)
            // The trigger should be skipped
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
        }

        @Test
        @DisplayName("Death trigger skips when no creatures survive (Wrath of God)")
        void deathTriggerSkipsWithNoCreatures() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — all creatures die

            GameData gd = harness.getGameData();

            // All creatures dead — no valid targets for "creature you control"
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
        }

        @Test
        @DisplayName("Ability fizzles when target creature is removed before resolution")
        void abilityFizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new GrizzlyBears());

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Elder Cathar dies

            // Choose target
            harness.handlePermanentChosen(player1, bearId);

            // Remove the target before the ability resolves
            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getId().equals(bearId));

            // Resolve — should fizzle
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("+1/+1 counters are permanent (persist through end of turn)")
        void countersArePermanent() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new GrizzlyBears());

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Elder Cathar dies

            harness.handlePermanentChosen(player1, bearId);
            harness.passBothPriorities(); // Resolve trigger

            // Advance to end step
            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // +1/+1 counters should still be there (they're counters, not temporary boosts)
            Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(bearId))
                    .findFirst().orElseThrow();
            assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(bears.getEffectivePower()).isEqualTo(3);
            assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        }
    }
}
