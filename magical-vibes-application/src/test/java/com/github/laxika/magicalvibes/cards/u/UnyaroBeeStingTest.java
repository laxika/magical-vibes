package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({UnyaroBeeSting.class, UnyaroGriffin.class, IronTuskElephant.class})
class UnyaroBeeStingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature, destroying a 2/2")
    void deals2DamageToCreature() {
        harness.addToBattlefield(player2, new UnyaroGriffin());
        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Unyaro Griffin");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Unyaro Griffin");
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature without destroying a 3/3")
    void deals2DamageToCreatureWithoutDestroyingIt() {
        harness.addToBattlefield(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Iron Tusk Elephant");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Iron Tusk Elephant");
    }
}
