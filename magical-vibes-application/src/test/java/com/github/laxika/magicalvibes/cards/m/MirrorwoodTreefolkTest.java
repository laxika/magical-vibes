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
import java.util.Map;

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

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
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

    @Test
    void redirectsSimultaneousDamageFromAllBlockers() {
        Permanent treefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        Permanent firstBlocker = addCreatureReady(player2, new AlphaKavu());
        addCreatureReady(player2, new AlphaKavu());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, treefolk), null, player2.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 2));

        assertThat(treefolk.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(treefolk);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void doesNotRedirectWhenDestinationHasLeftTheBattlefield() {
        Permanent treefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        Permanent destination = addCreatureReady(player2, new AlphaKavu());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, treefolk), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Singe(), new Singe(), new Singe()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, destination.getId());
        harness.castAndResolveInstant(player1, 0, destination.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destination);

        harness.castAndResolveInstant(player1, 0, treefolk.getId());

        assertThat(treefolk.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void redirectedDamageCanBeRedirectedByAnotherTreefolk() {
        Permanent firstTreefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        Permanent secondTreefolk = addCreatureReady(player1, new MirrorwoodTreefolk());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, firstTreefolk), null, secondTreefolk.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondTreefolk), null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, firstTreefolk.getId());

        assertThat(firstTreefolk.getMarkedDamage()).isZero();
        assertThat(secondTreefolk.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new MirrorwoodTreefolk());
        treefolk.tap();
        treefolk.setSummoningSick(true);
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
