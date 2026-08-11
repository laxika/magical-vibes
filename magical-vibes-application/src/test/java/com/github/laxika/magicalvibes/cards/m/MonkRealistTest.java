package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonkRealistTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Monk Realist");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithoutEtbTriggerWhenNoEnchantmentExists() {
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Monk Realist");
    }
}
