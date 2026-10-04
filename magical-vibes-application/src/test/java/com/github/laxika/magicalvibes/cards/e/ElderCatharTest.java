package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.c.ChampionOfTheParish;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.s.SomberwaldSpider;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardSubtype;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ElderCathar.class, WalkingCorpse.class, SomberwaldSpider.class, ChampionOfTheParish.class, BlasphemousAct.class})
class ElderCatharTest extends BaseCardTest {

    /**
     * Sets up combat where Elder Cathar (player1) attacks and is blocked by a 2/4 creature (player2).
     * Elder Cathar will die from combat damage.
     */
    private void setupCombatWhereElderCatharDies() {
        Permanent catharPerm = findPermanent(player1, "Elder Cathar");
        catharPerm.setSummoningSick(false);
        catharPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new SomberwaldSpider());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Nested
    @DisplayName("Death trigger")
    @CardUsed({ElderCathar.class, WalkingCorpse.class, SomberwaldSpider.class, ChampionOfTheParish.class, BlasphemousAct.class})
    class DeathTriggerTests {

        @Test
        @DisplayName("Human status is checked when the ability resolves")
        void creatureBecomingHumanBeforeResolutionGetsTwoCounters() {
            harness.addToBattlefield(player1, new ElderCathar());
            Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
            setupCombatWhereElderCatharDies();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, target.getId());

            target.getGrantedSubtypes().add(CardSubtype.HUMAN);
            harness.passBothPriorities();

            assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }

        @Test
        @DisplayName("A creature that stops being Human gets only one counter")
        void creatureLosingHumanBeforeResolutionGetsOneCounter() {
            harness.addToBattlefield(player1, new ElderCathar());
            Permanent target = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());
            setupCombatWhereElderCatharDies();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, target.getId());

            target.setTransientCreatureTypeOverride(CardSubtype.ZOMBIE);
            harness.passBothPriorities();

            assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("A target that changes to the opponent's control gets no counters")
        void targetChangingControllerBeforeResolutionIsIllegal() {
            harness.addToBattlefield(player1, new ElderCathar());
            Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
            setupCombatWhereElderCatharDies();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, target.getId());

            gd.playerBattlefields.get(player1.getId()).remove(target);
            gd.playerBattlefields.get(player2.getId()).add(target);
            harness.passBothPriorities();

            assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("When Elder Cathar dies, controller is prompted to choose a target creature they control")
        void deathTriggerPromptsForTarget() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new WalkingCorpse());
            setupCombatWhereElderCatharDies();

            harness.passBothPriorities(); // Combat damage - Elder Cathar dies

            GameData gd = harness.getGameData();

            // Elder Cathar should be dead
            harness.assertInGraveyard(player1, "Elder Cathar");

            // Controller should be prompted to choose a target creature
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Puts 1 +1/+1 counter on a non-Human creature")
        void putsOneCounterOnNonHuman() {
            harness.addToBattlefield(player1, new ElderCathar());
            WalkingCorpse bear = new WalkingCorpse();
            harness.addToBattlefield(player1, bear);

            UUID bearId = harness.getPermanentId(player1, "Walking Corpse");

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Combat damage - Elder Cathar dies

            // Choose the non-Human Walking Corpse
            harness.handlePermanentChosen(player1, bearId);

            // Triggered ability should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

            // Resolve the triggered ability
            harness.passBothPriorities();

            // Walking Corpse should have 1 +1/+1 counter
            Permanent bearsPerm = findPermanent(player1, "Walking Corpse");
            assertThat(bearsPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
            harness.passBothPriorities(); // Combat damage - Elder Cathar dies

            // Choose the Human Champion of the Parish
            harness.handlePermanentChosen(player1, championId);

            // Resolve the triggered ability
            harness.passBothPriorities();

            // Champion of the Parish should have 2 +1/+1 counters (the "instead" clause)
            Permanent championPerm = findPermanent(player1, "Champion of the Parish");
            assertThat(championPerm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target opponent's creature (targets creature you control)")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player1, new ElderCathar());
            // Only opponent has a creature (besides the blocker)
            // No creatures on player1's side other than the dying Elder Cathar

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Combat damage - Elder Cathar dies

            GameData gd = harness.getGameData();

            // No valid targets (player1 has no creatures, can't target opponent's)
            // The trigger should be skipped
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid targets"));
        }

        @Test
        @DisplayName("Death trigger skips when no creatures survive (Blasphemous Act)")
        void deathTriggerSkipsWithNoCreatures() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new WalkingCorpse());

            harness.setHand(player1, List.of(new BlasphemousAct()));
            harness.addMana(player1, ManaColor.RED, 7);

            harness.castSorcery(player1, 0);
            harness.passBothPriorities(); // Resolve Blasphemous Act - all creatures die

            GameData gd = harness.getGameData();

            // All creatures dead - no valid targets for "creature you control"
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid targets"));
        }

        @Test
        @DisplayName("Ability fizzles when target creature is removed before resolution")
        void abilityFizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new WalkingCorpse());

            UUID bearId = harness.getPermanentId(player1, "Walking Corpse");

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Elder Cathar dies

            // Choose target
            harness.handlePermanentChosen(player1, bearId);

            // Remove the target before the ability resolves
            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getId().equals(bearId));

            // Resolve - should fizzle
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("+1/+1 counters are permanent (persist through end of turn)")
        void countersArePermanent() {
            harness.addToBattlefield(player1, new ElderCathar());
            harness.addToBattlefield(player1, new WalkingCorpse());

            UUID bearId = harness.getPermanentId(player1, "Walking Corpse");

            setupCombatWhereElderCatharDies();
            harness.passBothPriorities(); // Elder Cathar dies

            harness.handlePermanentChosen(player1, bearId);
            harness.passBothPriorities(); // Resolve trigger

            // Advance to end step
            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            // +1/+1 counters should still be there (they're counters, not temporary boosts)
            Permanent bears = findPermanent(player1, "Walking Corpse");
            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(bears.getEffectivePower()).isEqualTo(3);
            assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        }
    }
}
