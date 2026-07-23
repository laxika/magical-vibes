package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MusicianTest extends BaseCardTest {

    private Permanent addReadyMusician(Player player) {
        return addCreatureReady(player, new Musician());
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Tap ability puts a music counter and grants the destroy-unless-pay upkeep ability")
    void putsMusicCounterAndGrantsAbility() {
        Permanent musician = addReadyMusician(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int musicianIdx = gd.playerBattlefields.get(player1.getId()).indexOf(musician);
        harness.activateAbility(player1, musicianIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MUSIC)).isEqualTo(1);
        assertThat(bears.getPersistentTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED))
                .containsExactly(new DestroyUnlessPaysPerCounterEffect(CounterType.MUSIC, "{1}"));
        assertThat(musician.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second activation adds another music counter but does not re-grant the ability")
    void secondActivationOnlyAddsCounter() {
        Permanent musician = addReadyMusician(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int musicianIdx = gd.playerBattlefields.get(player1.getId()).indexOf(musician);
        harness.activateAbility(player1, musicianIdx, null, bears.getId());
        harness.passBothPriorities();

        musician.untap();
        harness.activateAbility(player1, musicianIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MUSIC)).isEqualTo(2);
        assertThat(bears.getPersistentTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
    }

    @Test
    @DisplayName("Paying music upkeep keeps the creature; cost scales with counters")
    void payingMusicUpkeepKeepsCreature() {
        Permanent musician = addReadyMusician(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int musicianIdx = gd.playerBattlefields.get(player1.getId()).indexOf(musician);
        harness.activateAbility(player1, musicianIdx, null, bears.getId());
        harness.passBothPriorities();
        musician.untap();
        harness.activateAbility(player1, musicianIdx, null, bears.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Declining music upkeep destroys the creature")
    void decliningMusicUpkeepDestroys() {
        Permanent musician = addReadyMusician(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int musicianIdx = gd.playerBattlefields.get(player1.getId()).indexOf(musician);
        harness.activateAbility(player1, musicianIdx, null, bears.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Granted music ability persists after Musician leaves")
    void abilityPersistsAfterMusicianLeaves() {
        Permanent musician = addReadyMusician(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int musicianIdx = gd.playerBattlefields.get(player1.getId()).indexOf(musician);
        harness.activateAbility(player1, musicianIdx, null, bears.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(musician);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent musician = addReadyMusician(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.p.Plains());

        int musicianIdx = gd.playerBattlefields.get(player1.getId()).indexOf(musician);
        assertThatThrownBy(() -> harness.activateAbility(player1, musicianIdx, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays own cumulative upkeep {1}")
    void paysOwnCumulativeUpkeep() {
        Permanent musician = harness.addToBattlefieldAndReturn(player1, new Musician());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(musician.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(musician);
    }
}
