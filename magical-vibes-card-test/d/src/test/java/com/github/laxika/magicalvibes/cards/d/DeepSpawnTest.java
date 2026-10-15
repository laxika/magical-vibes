package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.h.HomaridWarrior;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepSpawn.class, HomaridWarrior.class})
class DeepSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Milling two cards keeps Deep Spawn")
    void millingTwoCardsKeepsDeepSpawn() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of(new HomaridWarrior(), new HomaridWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Homarid Warrior", "Homarid Warrior");
    }

    @Test
    @DisplayName("Declining to mill sacrifices Deep Spawn")
    void decliningToMillSacrificesDeepSpawn() {
        addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of(new HomaridWarrior(), new HomaridWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Deep Spawn");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Fewer than two cards makes the upkeep cost unavailable")
    void tooFewCardsSacrificesWithoutPrompt() {
        addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of(new HomaridWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Deep Spawn");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Activating grants shroud, taps itself, and skips next untap")
    void activatingGrantsShroudTapsAndSkipsUntap() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spawn.getGrantedKeywords()).contains(Keyword.SHROUD);
        assertThat(spawn.isTapped()).isTrue();
        assertThat(spawn.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability keeps Deep Spawn tapped through the next untap step")
    void activationSkipsNextUntapStep() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of(new HomaridWarrior(), new HomaridWarrior()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        assertThat(spawn.isTapped()).isTrue();
        assertThat(spawn.getSkipUntapCount()).isZero();

        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        Permanent blocker = addCreatureReady(player2, new HomaridWarrior());

        spawn.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 3
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(spawn.getGrantedKeywords()).contains(Keyword.SHROUD);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spawn.getGrantedKeywords()).doesNotContain(Keyword.SHROUD);
    }

    @Test
    @DisplayName("An empty library cannot pay the upkeep cost")
    void emptyLibrarySacrificesWithoutPrompt() {
        addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Deep Spawn");
        harness.assertInGraveyard(player1, "Deep Spawn");
    }

    @Test
    @DisplayName("Repeated activations while tapped only prevent the next untap")
    void repeatedActivationsDoNotSkipAdditionalUntapSteps() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(spawn.isTapped()).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(spawn.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(spawn.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Changing control does not prevent the new controller's untap")
    void changingControlDoesNotMoveUntapRestrictionToNewController() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), spawn,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        harness.performUntapStep(player2);

        assertThat(spawn.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The original activator's untap step expires the restriction after control changes")
    void restrictionExpiresDuringOriginalActivatorsUntapStep() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), spawn,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);

        assertThat(spawn.isTapped()).isFalse();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
