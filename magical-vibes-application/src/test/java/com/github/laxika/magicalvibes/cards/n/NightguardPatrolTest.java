package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NightguardPatrol.class)
class NightguardPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike and vigilance on the battlefield")
    void hasFirstStrikeAndVigilance() {
        harness.addToBattlefield(player1, new NightguardPatrol());

        Permanent patrol = findPermanent(player1, "Nightguard Patrol");

        assertThat(gqs.hasKeyword(gd, patrol, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attacking does not tap Nightguard Patrol")
    void attackingDoesNotTapNightguardPatrol() {
        Permanent patrol = addCreatureReady(player1, new NightguardPatrol());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(patrol)));

        assertThat(patrol.isTapped()).isFalse();
    }
}
