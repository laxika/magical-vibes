package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarriorAngelTest extends BaseCardTest {

    @Test
    void gainsLifeEqualToDamageDealtToOpponent() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        Permanent angel = addCreatureReady(player1, new WarriorAngel());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(angel)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
