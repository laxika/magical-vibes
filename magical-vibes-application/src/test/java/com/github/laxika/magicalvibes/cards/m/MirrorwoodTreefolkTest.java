package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.s.Singe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorwoodTreefolk.class, AlphaKavu.class, Singe.class, CloudCover.class})
class MirrorwoodTreefolkTest extends BaseCardTest {

    @Test
    void redirectsTheEntireNextDamageEvent() {
        Permanent treefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        Permanent destination = addReadyStats(player1, 4, 4);
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, treefolk), null, destination.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, treefolk), 0)));
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(2);

        harness.setHand(player2, List.of(new Singe()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, treefolk.getId());

        assertThat(treefolk.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void redirectsDamageToAPlayer() {
        Permanent treefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, treefolk), null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, treefolk.getId());

        assertThat(treefolk.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void doesNotAllowNonTargetPermanentAsDestination() {
        Permanent treefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new CloudCover());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, treefolk), null,
                enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shieldExpiresAtEndOfTurn() {
        Permanent treefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, treefolk), null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Singe()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, treefolk.getId());

        assertThat(treefolk.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        AlphaKavu card = new AlphaKavu();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
