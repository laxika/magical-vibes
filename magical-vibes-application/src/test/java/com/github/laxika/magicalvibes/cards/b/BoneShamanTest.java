package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.w.WallOfEssence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoneShamanTest extends BaseCardTest {

    @Test
    @DisplayName("After the ability resolves, a creature Bone Shaman damages can't regenerate and dies")
    void damagedCreatureCannotRegenerate() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        shaman.setAttacking(true);
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        activateAbility();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Damage dealt earlier this turn is covered when the ability resolves later")
    void earlierDamageIsCoveredWhenAbilityResolvesLater() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        shaman.setAttacking(true);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent wall = addCreatureReady(player2, new WallOfEssence());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        wall.setRegenerationShield(1);
        activateAbility();
        harness.activateAbility(player1, 1, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Essence");
        harness.assertInGraveyard(player2, "Wall of Essence");
        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without the ability, a creature Bone Shaman damages regenerates normally")
    void withoutAbilityDamagedCreatureRegenerates() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        shaman.setAttacking(true);
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        assertThat(skeletons.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A creature damaged by another source still regenerates while the ability is active")
    void damageFromAnotherSourceStillRegenerates() {
        addCreatureReady(player1, new BoneShaman());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        activateAbility();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("The ability stops applying after end-of-turn cleanup")
    void abilityWearsOffAtEndOfTurn() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        activateAbility();
        assertThat(shaman.isDamagedCreaturesCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shaman.isDamagedCreaturesCantRegenerateThisTurn()).isFalse();
    }

    private void activateAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
