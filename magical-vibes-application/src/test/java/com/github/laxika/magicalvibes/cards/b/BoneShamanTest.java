package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.o.OrcishCannoneers;
import com.github.laxika.magicalvibes.cards.w.WallOfShields;
import com.github.laxika.magicalvibes.cards.y.YavimayaGnats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneShaman.class, YavimayaGnats.class, WallOfShields.class,
        OrcishCannoneers.class, BalduvianBears.class})
class BoneShamanTest extends BaseCardTest {

    @Test
    @DisplayName("After the ability resolves, a creature Bone Shaman damages can't regenerate and dies")
    void damagedCreatureCannotRegenerate() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        shaman.setAttacking(true);
        Permanent gnats = addCreatureReady(player2, new YavimayaGnats());
        gnats.setRegenerationShield(1);

        activateAbility();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Yavimaya Gnats");
        harness.assertInGraveyard(player2, "Yavimaya Gnats");
    }

    @Test
    @DisplayName("A regeneration ability activated before combat cannot save a creature damaged by Bone Shaman")
    void regenerationAbilityIsDenied() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        shaman.setAttacking(true);
        addCreatureReady(player2, new YavimayaGnats());

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        activateAbility();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Yavimaya Gnats");
        harness.assertInGraveyard(player2, "Yavimaya Gnats");
    }

    @Test
    @DisplayName("Damage dealt earlier this turn is covered when the ability resolves later")
    void earlierDamageIsCoveredWhenAbilityResolvesLater() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        shaman.setAttacking(true);
        Permanent cannoneers = addCreatureReady(player1, new OrcishCannoneers());
        Permanent wall = addCreatureReady(player2, new WallOfShields());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        wall.setRegenerationShield(1);
        activateAbility();
        harness.activateAbility(player1, 1, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Shields");
        harness.assertInGraveyard(player2, "Wall of Shields");
        assertThat(cannoneers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without the ability, a creature Bone Shaman damages regenerates normally")
    void withoutAbilityDamagedCreatureRegenerates() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
        shaman.setAttacking(true);
        Permanent gnats = addCreatureReady(player2, new YavimayaGnats());
        gnats.setRegenerationShield(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Yavimaya Gnats");
        assertThat(gnats.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A creature damaged by another source still regenerates while the ability is active")
    void damageFromAnotherSourceStillRegenerates() {
        addCreatureReady(player1, new BoneShaman());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.setAttacking(true);
        Permanent gnats = addCreatureReady(player2, new YavimayaGnats());
        gnats.setRegenerationShield(1);

        activateAbility();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Yavimaya Gnats");
    }

    @Test
    @DisplayName("The ability stops applying after end-of-turn cleanup")
    void abilityWearsOffAtEndOfTurn() {
        Permanent shaman = addCreatureReady(player1, new BoneShaman());
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
