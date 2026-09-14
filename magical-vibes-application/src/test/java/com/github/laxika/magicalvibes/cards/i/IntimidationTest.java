package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CateranBrute;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Intimidation.class, FreshVolunteers.class, CateranBrute.class, HengeGuardian.class})
class IntimidationTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have fear")
    void ownCreaturesHaveFear() {
        Permanent volunteers = addCreatureReady(player1, new FreshVolunteers());
        harness.addToBattlefield(player1, new Intimidation());

        assertThat(gqs.hasKeyword(gd, volunteers, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Creatures you do not control do not gain fear")
    void opponentCreaturesDoNotHaveFear() {
        Permanent opponentVolunteers = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new Intimidation());

        assertThat(gqs.hasKeyword(gd, opponentVolunteers, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Fear is lost when Intimidation leaves the battlefield")
    void fearIsLostWhenSourceLeaves() {
        Permanent volunteers = addCreatureReady(player1, new FreshVolunteers());
        Permanent intimidation = harness.addToBattlefieldAndReturn(player1, new Intimidation());
        assertThat(gqs.hasKeyword(gd, volunteers, Keyword.FEAR)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(intimidation);

        assertThat(gqs.hasKeyword(gd, volunteers, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void fearPreventsNonBlackNonArtifactCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        harness.addToBattlefield(player1, new Intimidation());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void fearAllowsBlackCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        harness.addToBattlefield(player1, new Intimidation());
        Permanent blocker = addCreatureReady(player2, new CateranBrute());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void fearAllowsArtifactCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        harness.addToBattlefield(player1, new Intimidation());
        Permanent blocker = addCreatureReady(player2, new HengeGuardian());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
