package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PouncingLynx.class)
class PouncingLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllersTurn() {
        Permanent lynx = addCreatureReady(player1, new PouncingLynx());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, lynx, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during its controller's opponent's turn")
    void noFirstStrikeDuringOpponentsTurn() {
        Permanent lynx = addCreatureReady(player1, new PouncingLynx());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, lynx, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike updates as the active player changes for both controllers")
    void firstStrikeUpdatesWithActivePlayer() {
        Permanent firstLynx = addCreatureReady(player1, new PouncingLynx());
        Permanent secondLynx = addCreatureReady(player2, new PouncingLynx());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, firstLynx, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondLynx, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, firstLynx, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondLynx, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, firstLynx, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondLynx, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attacking Lynx kills a defending Lynx before it can deal damage")
    void attackingLynxDealsFirstStrikeDamage() {
        Permanent attacker = addCreatureReady(player1, new PouncingLynx());
        addCreatureReady(player2, new PouncingLynx());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Pouncing Lynx");
        harness.assertInGraveyard(player2, "Pouncing Lynx");
        harness.assertLife(player2, 20);
    }
}
