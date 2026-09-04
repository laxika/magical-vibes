package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SabretoothTiger.class, BalduvianBears.class})
class SabretoothTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike on the battlefield")
    void hasFirstStrikeOnBattlefield() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player1, new SabretoothTiger());

        assertThat(gqs.hasKeyword(gd, tiger, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike kills a 2/2 blocker before regular combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new SabretoothTiger());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sabretooth Tiger");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }
}
