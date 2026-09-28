package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BorosGuildmage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightguardPatrol.class, BorosGuildmage.class})
class NightguardPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("First strike destroys a 2/2 blocker before it can deal combat damage")
    void firstStrikeDealsCombatDamageFirst() {
        addCreatureReady(player1, new NightguardPatrol());
        addCreatureReady(player2, new BorosGuildmage());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Nightguard Patrol");
        harness.assertInGraveyard(player2, "Boros Guildmage");
    }

    @Test
    @DisplayName("Attacking does not tap Nightguard Patrol")
    void attackingDoesNotTapNightguardPatrol() {
        Permanent patrol = addCreatureReady(player1, new NightguardPatrol());

        declareAttackers(List.of(0));

        assertThat(patrol.isTapped()).isFalse();
    }
}
