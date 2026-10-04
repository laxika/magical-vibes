package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSoothsayer.class, GoblinEliteInfantry.class, FeralShadow.class, ViashinoWarrior.class,
        BoggartShenanigans.class})
class GoblinSoothsayerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability boosts red creatures on both sides but not non-red creatures")
    void boostsRedCreaturesOnly() {
        addCreatureReady(player1, new GoblinSoothsayer());
        Permanent ownWarrior = addCreatureReady(player1, new ViashinoWarrior());
        Permanent enemyWarrior = addCreatureReady(player2, new ViashinoWarrior());
        Permanent enemyShadow = addCreatureReady(player2, new FeralShadow());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownWarrior)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownWarrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enemyWarrior)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enemyWarrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enemyShadow)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyShadow)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new GoblinSoothsayer());
        Permanent warrior = addCreatureReady(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing itself as the Goblin still resolves the boost")
    void canSacrificeItself() {
        addCreatureReady(player1, new GoblinSoothsayer());
        Permanent warrior = addCreatureReady(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Soothsayer");
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can sacrifice another Goblin, leaving the tapped source boosted")
    void canSacrificeAnotherGoblin() {
        Permanent soothsayer = addCreatureReady(player1, new GoblinSoothsayer());
        Permanent fodder = addCreatureReady(player1, new GoblinEliteInfantry());
        Permanent redCreature = addCreatureReady(player1, new ViashinoWarrior());
        Permanent nonRedCreature = addCreatureReady(player1, new FeralShadow());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(soothsayer, redCreature, nonRedCreature)
                .doesNotContain(fodder);
        assertThat(soothsayer.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, soothsayer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soothsayer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, redCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonRedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonRedCreature)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new GoblinSoothsayer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new GoblinSoothsayer());
        addCreatureReady(player1, new GoblinEliteInfantry());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Goblin Soothsayer");
        harness.assertOnBattlefield(player1, "Goblin Elite Infantry");
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and creatures are selected at resolution")
    void selectsCreaturesAtResolution() {
        addCreatureReady(player1, new GoblinSoothsayer());
        Permanent warrior = addCreatureReady(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Goblin Soothsayer");
        harness.assertInGraveyard(player1, "Goblin Soothsayer");
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new ViashinoWarrior());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);

        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new ViashinoWarrior());
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice a noncreature Goblin permanent")
    void canSacrificeKindredGoblin() {
        Permanent soothsayer = addCreatureReady(player1, new GoblinSoothsayer());
        Permanent shenanigans = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        Permanent warrior = addCreatureReady(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, shenanigans.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Soothsayer");
        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        assertThat(soothsayer.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, soothsayer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soothsayer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
    }
}
