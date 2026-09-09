package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValakutInvoker.class, FyndhornElves.class})
class ValakutInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player for eight mana")
    void dealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals 3 damage to target creature")
    void dealsThreeDamageToCreature() {
        addReadyInvoker(player1);
        harness.addToBattlefield(player2, new FyndhornElves());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        UUID targetId = harness.getPermanentId(player2, "Fyndhorn Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fyndhorn Elves");
        harness.assertInGraveyard(player2, "Fyndhorn Elves");
    }

    private Permanent addReadyInvoker(Player player) {
        return addCreatureReady(player, new ValakutInvoker());
    }
}
