package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BounceOff;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.ZealousConscripts;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossessionEngine.class, GrizzlyBears.class, HillGiant.class,
        ZealousConscripts.class, BounceOff.class, WordOfSeizing.class})
class PossessionEngineTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains control of and locks the target creature")
    void entersAndTakesControlOfTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castPossessionEngine(target.getId());

        Permanent engine = findPermanent(player1, "Possession Engine");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.newestControlEffectFor(target.getId()).sourcePermanentId())
                .isEqualTo(engine.getId());
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
    }

    @Test
    @DisplayName("Crew 3 animates Possession Engine and taps the crew")
    void crewAnimatesEngineAndTapsCrew() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new PossessionEngine());
        engine.setSummoningSick(false);
        Permanent crew = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, engine)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The control and lock end when another player gains control of the Vehicle")
    void controlChangeEndsTheEffect() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castPossessionEngine(target.getId());
        Permanent engine = findPermanent(player1, "Possession Engine");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ZealousConscripts()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castCreature(player2, 0, 0, engine.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(engine, target);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    private void castPossessionEngine(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PossessionEngine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0, targetId);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Returning the Vehicle ends both control and the combat lock")
    void returningEngineEndsBothEffects() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castPossessionEngine(target.getId());
        Permanent engine = findPermanent(player1, "Possession Engine");

        bounce(player1, engine);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(engine, target);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    @Test
    @DisplayName("Leaving before the enters ability resolves prevents both effects")
    void engineLeavesBeforeTriggerResolves() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castEngineLeavingTriggerOnStack(target);
        Permanent engine = findPermanent(player1, "Possession Engine");

        bounce(player1, engine);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    @Test
    @DisplayName("Losing and regaining the Vehicle before resolution cannot start either effect")
    void controlLostAndRegainedBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castEngineLeavingTriggerOnStack(target);
        Permanent engine = findPermanent(player1, "Possession Engine");

        seize(player2, engine);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(engine);
        seize(player1, engine);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(engine);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    @Test
    @DisplayName("Returning the stolen creature does not transfer its lock to a new permanent")
    void returnedCreatureCanBeRecastWithoutLock() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castPossessionEngine(target.getId());

        bounce(player1, target);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(gqs.isLockedFromAttacking(gd, returned.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, returned.getId())).isFalse();
    }

    @Test
    @DisplayName("A stolen creature can crew the Vehicle despite its combat lock")
    void lockedCreatureCanCrewEngineUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        castPossessionEngine(target.getId());
        Permanent engine = findPermanent(player1, "Possession Engine");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(engine), null, null);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, engine)).isTrue();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, engine)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
    }

    @Test
    @DisplayName("Crew cannot be paid with less than three total power")
    void insufficientCrewPowerIsRejected() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new PossessionEngine());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, engine)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castEngineLeavingTriggerOnStack(Permanent target) {
        harness.setHand(player1, List.of(new PossessionEngine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
    }

    private void bounce(Player player, Permanent target) {
        harness.setHand(player, List.of(new BounceOff()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }

    private void seize(Player player, Permanent target) {
        harness.setHand(player, List.of(new WordOfSeizing()));
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }
}
