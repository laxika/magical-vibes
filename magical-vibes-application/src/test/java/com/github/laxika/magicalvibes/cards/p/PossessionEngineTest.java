package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.ZealousConscripts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent engine = addEngineReady(player1);
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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(engine, target);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    private void castPossessionEngine(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PossessionEngine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addEngineReady(Player player) {
        Permanent engine = new Permanent(new PossessionEngine());
        engine.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(engine);
        return engine;
    }
}
