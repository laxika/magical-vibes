package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinChariot.class})
class GoblinChariotTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Goblin Chariot attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.castFromHand(player1, new GoblinChariot(), "{2}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent chariot = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chariot.isAttackedThisTurn()).isTrue();
        assertThat(chariot.isTapped()).isTrue();
    }
}
