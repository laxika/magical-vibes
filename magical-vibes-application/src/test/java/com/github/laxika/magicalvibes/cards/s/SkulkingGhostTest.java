package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkulkingGhostTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new SkulkingGhost());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, ghost.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ghost.getId()));
        harness.assertInGraveyard(player1, "Skulking Ghost");
    }

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new SkulkingGhost());

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = findPermanent(player2, "Icy Manipulator");
        icy.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, ghost.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ghost.getId()));
        harness.assertInGraveyard(player1, "Skulking Ghost");
    }

    @Test
    @DisplayName("Stays on the battlefield when nothing targets it")
    void staysWhenNotTargeted() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new SkulkingGhost());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(ghost.getId()));
    }
}
