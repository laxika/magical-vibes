package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OranRiefSurvivalist;
import com.github.laxika.magicalvibes.cards.k.KazanduRefuge;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrailblazersBoots.class, OranRiefSurvivalist.class, KazanduRefuge.class, Forest.class})
class TrailblazersBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature cannot be blocked while defending player controls a nonbasic land")
    void cannotBeBlockedWithNonbasicLand() {
        harness.addToBattlefield(player2, new KazanduRefuge());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new TrailblazersBoots());
        boots.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        prepareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Equipped creature can be blocked while defending player controls only basic lands")
    void canBeBlockedWithBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new TrailblazersBoots());
        boots.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        prepareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unattached Boots do not grant nonbasic landwalk")
    void losesLandwalkWhenUnattached() {
        harness.addToBattlefield(player2, new KazanduRefuge());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new TrailblazersBoots());
        boots.setAttachedTo(attacker.getId());
        boots.setAttachedTo(null);
        attacker.setAttacking(true);

        prepareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void equipResolvesAndCanMoveToAnotherCreature() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new TrailblazersBoots());
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        assertThat(boots.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(boots.getAttachedTo()).isEqualTo(first.getId());

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        assertThat(boots.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackersOwnNonbasicLandDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new KazanduRefuge());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new TrailblazersBoots());
        boots.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        prepareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void nonlandPermanentDoesNotPreventBlocking() {
        harness.addToBattlefield(player2, new TrailblazersBoots());
        Permanent blocker = addCreature(player2);
        Permanent attacker = addCreature(player1);
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new TrailblazersBoots());
        boots.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        prepareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new OranRiefSurvivalist());
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
