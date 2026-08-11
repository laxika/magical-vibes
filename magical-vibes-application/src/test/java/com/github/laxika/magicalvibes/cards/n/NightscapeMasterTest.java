package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        Permanent source = addReadyMaster(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Centaur Courser");
        harness.assertInHand(player2, "Centaur Courser");
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability deals 2 damage to a target creature")
    void dealsTwoDamageToTargetCreature() {
        Permanent source = addReadyMaster(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Neither ability can target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent source = addReadyMaster(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                0,
                null,
                island.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                1,
                null,
                island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMaster(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NightscapeMaster());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
