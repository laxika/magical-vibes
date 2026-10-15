package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGhoulGunslinger.class, WalkingCorpse.class, GrizzlyBears.class, Forest.class, AmoeboidChangeling.class})
class TheGhoulGunslingerTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken Zombie death gives two rad counters and creates a Treasure when you are targeted")
    void qualifyingDeathGivesRadAndTreasureWhenTargetingController() {
        addCreatureReady(player1, new TheGhoulGunslinger());
        Permanent zombie = addCreatureReady(player1, new WalkingCorpse());

        destroyAndResolve(zombie, player1);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("The Treasure clause does not apply when an opponent is targeted")
    void opponentTargetGetsRadButNoTreasure() {
        addCreatureReady(player1, new TheGhoulGunslinger());
        Permanent zombie = addCreatureReady(player1, new WalkingCorpse());

        destroyAndResolve(zombie, player2);

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Rad counters mill in the precombat main phase and remove one per nonland card")
    void radCountersResolveInPrecombatMain() {
        gd.playerRadCounters.put(player1.getId(), 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    void ownDeathTriggersExactlyOnce() {
        Permanent ghoul = addCreatureReady(player1, new TheGhoulGunslinger());

        destroyAndResolve(ghoul, player1);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tokenCopyTriggersForItsOwnDeath() {
        Card token = new TheGhoulGunslinger().createRuntimeCopy();
        token.setToken(true);
        Permanent ghoul = addCreatureReady(player1, token);

        destroyAndResolve(ghoul, player1);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void anotherTokenZombieDoesNotTrigger() {
        addCreatureReady(player1, new TheGhoulGunslinger());
        Card token = new WalkingCorpse().createRuntimeCopy();
        token.setToken(true);
        Permanent zombie = addCreatureReady(player1, token);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, zombie));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void opponentsZombieDoesNotTrigger() {
        addCreatureReady(player1, new TheGhoulGunslinger());
        Permanent zombie = addCreatureReady(player2, new WalkingCorpse());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, zombie));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureWithoutEitherTypeDoesNotTrigger() {
        addCreatureReady(player1, new TheGhoulGunslinger());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, bear));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void changelingDeathTriggersOnlyOnceDespiteHavingBothTypes() {
        addCreatureReady(player1, new TheGhoulGunslinger());
        Permanent changeling = addCreatureReady(player1, new AmoeboidChangeling());

        destroyAndResolve(changeling, player1);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureThatGainedZombieAndMutantTypesTriggersOnDeath() {
        addCreatureReady(player1, new AmoeboidChangeling());
        addCreatureReady(player1, new TheGhoulGunslinger());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        destroyAndResolve(bear, player1);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void zombieThatLostAllCreatureTypesDoesNotTriggerOnDeath() {
        addCreatureReady(player1, new AmoeboidChangeling());
        addCreatureReady(player1, new TheGhoulGunslinger());
        Permanent zombie = addCreatureReady(player1, new WalkingCorpse());
        harness.activateAbility(player1, 0, 1, null, zombie.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, zombie));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private void destroyAndResolve(Permanent permanent, com.github.laxika.magicalvibes.model.Player targetPlayer) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, permanent));
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, targetPlayer.getId());
        }
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, targetPlayer.getId());
            harness.passBothPriorities();
        }
    }
}
