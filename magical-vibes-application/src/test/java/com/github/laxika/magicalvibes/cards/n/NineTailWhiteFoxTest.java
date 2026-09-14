package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NineTailWhiteFox.class, Island.class})
class NineTailWhiteFoxTest extends BaseCardTest {

    @Test
    void drawsCardWhenItDealsCombatDamageToAPlayer() {
        Island drawnCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent fox = new Permanent(new NineTailWhiteFox());
        fox.setSummoningSick(false);
        fox.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(fox);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
