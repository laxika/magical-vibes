package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndeadSlayer.class, ScatheZombies.class, GrizzlyBears.class, DrudgeSkeletons.class, ChildOfNight.class})
class UndeadSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target Zombie")
    void exilesTargetZombie() {
        addCreatureReady(player1, new UndeadSlayer());
        Permanent zombie = addCreatureReady(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"));
    }

    @Test
    @DisplayName("Cannot target non-Skeleton/Vampire/Zombie creature")
    void cannotTargetNonUndeadCreature() {
        addCreatureReady(player1, new UndeadSlayer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new UndeadSlayer());
        Permanent zombie = addCreatureReady(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new UndeadSlayer());
        Permanent zombie = addCreatureReady(player2, new ScatheZombies());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new UndeadSlayer());
        Permanent zombie = addCreatureReady(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exilesTargetSkeleton() {
        addCreatureReady(player1, new UndeadSlayer());
        Permanent skeleton = addCreatureReady(player2, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, skeleton.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void exilesOwnVampireAndPaysTapCost() {
        Permanent slayer = addCreatureReady(player1, new UndeadSlayer());
        Permanent vampire = addCreatureReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, vampire.getId());

        assertThat(slayer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vampire.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Child of Night");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Child of Night"));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
