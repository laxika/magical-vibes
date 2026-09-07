package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkulkingGhost.class, Shock.class, IcyManipulator.class, MtendaHerder.class})
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

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, ghost.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ghost.getId()));
        harness.assertInGraveyard(player1, "Skulking Ghost");
    }

    @Test
    @DisplayName("Sacrifices itself when targeted by its controller's activated ability")
    void sacrificesWhenTargetedByOwnAbility() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new SkulkingGhost());

        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icy.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(icy), null,
                ghost.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Ghost");
        harness.assertInGraveyard(player1, "Skulking Ghost");
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking it")
    void flyingPreventsGroundBlocker() {
        Permanent ghost = addCreatureReady(player1, new SkulkingGhost());
        Permanent blocker = addCreatureReady(player2, new MtendaHerder());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(ghost)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Stays on the battlefield when nothing targets it")
    void staysWhenNotTargeted() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new SkulkingGhost());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(ghost.getId()));
    }
}
