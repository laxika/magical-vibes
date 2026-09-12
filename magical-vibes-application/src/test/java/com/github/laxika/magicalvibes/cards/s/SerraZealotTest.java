package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IntrepidHero;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraZealot.class, IntrepidHero.class})
class SerraZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike on the battlefield")
    void hasFirstStrikeOnBattlefield() {
        harness.addToBattlefield(player1, new SerraZealot());

        Permanent zealot = findPermanent(player1, "Serra Zealot");

        assertThat(gqs.hasKeyword(gd, zealot, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike kills a 1/1 blocker before regular combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new SerraZealot());
        attacker.setAttacking(true);

        addCreatureReady(player2, new IntrepidHero());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Zealot");
        harness.assertInGraveyard(player2, "Intrepid Hero");
    }
}
