package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoratamiMirrorMageTest extends BaseCardTest {

    @Test
    @DisplayName("Returns three lands as a cost and bounces the target creature")
    void bouncesTargetCreature() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Island"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate with only two lands to return")
    void cannotActivateWithTwoLands() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land is an illegal target")
    void rejectsLandTarget() {
        harness.addToBattlefield(player1, new SoratamiMirrorMage());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Soratami Mirror-Mage"), 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
