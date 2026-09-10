package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.p.Propaganda;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RatsOfRath.class, CursedScroll.class, MoggConscripts.class, Propaganda.class, Wasteland.class})
class RatsOfRathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature its controller controls")
    void destroysOwnCreature() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent creature = addCreatureReady(player1, new MoggConscripts());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Destroys an artifact and a land its controller controls")
    void destroysOwnArtifactAndLand() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedScroll());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Wasteland());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact, land);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent enemy = addCreatureReady(player2, new MoggConscripts());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enemy);
    }

    @Test
    @DisplayName("Cannot target a non-artifact, noncreature, nonland permanent you control")
    void cannotTargetOtherOwnPermanent() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Propaganda());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land you control");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchantment);
    }

    @Test
    @DisplayName("Fizzles if the target is no longer controlled when the ability resolves")
    void fizzlesIfTargetIsNoLongerControlled() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent creature = addCreatureReady(player1, new MoggConscripts());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }
}
