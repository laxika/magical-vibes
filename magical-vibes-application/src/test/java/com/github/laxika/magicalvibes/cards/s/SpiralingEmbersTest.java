package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiralingEmbersTest extends BaseCardTest {

    @Test
    void dealsDamageToPlayerEqualToControllerHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpiralingEmbers(), new Island(), new Plains(), new GrizzlyBears()));
        addMana(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void usesControllerHandSizeOnResolutionAndCanTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new SpiralingEmbers(), new Island()));
        addMana(player1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerHands.get(player1.getId()).add(new Plains());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 4);
    }
}
