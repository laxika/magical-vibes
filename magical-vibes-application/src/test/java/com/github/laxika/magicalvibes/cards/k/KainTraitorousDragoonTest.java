package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KainTraitorousDragoon.class, Forest.class})
class KainTraitorousDragoonTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during its controller's turn only")
    void flyingIsLimitedToControllerTurn() {
        Permanent kain = addCreatureReady(player1, new KainTraitorousDragoon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, kain, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, kain, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Transfers Kain and gives its controller cards, tapped Treasures, and life loss equal to damage")
    void transfersAndResolvesCombatDamageRider() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent kain = addCreatureReady(player1, new KainTraitorousDragoon());
        kain.setAttacking(true);

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kain);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
    }
}
