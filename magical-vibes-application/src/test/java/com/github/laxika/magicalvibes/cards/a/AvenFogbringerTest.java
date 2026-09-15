package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenFogbringer.class, KrosanVerge.class, SuntailHawk.class})
class AvenFogbringerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns the targeted land to its owner's hand")
    void etbReturnsTargetedLand() {
        harness.addToBattlefield(player2, new KrosanVerge());
        UUID targetId = harness.getPermanentId(player2, "Krosan Verge");
        castAvenFogbringer(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Krosan Verge");
        harness.assertInHand(player2, "Krosan Verge");
        harness.assertOnBattlefield(player1, "Aven Fogbringer");
    }

    @Test
    @DisplayName("ETB can target a land its controller owns")
    void etbReturnsOwnLand() {
        harness.addToBattlefield(player1, new KrosanVerge());
        UUID targetId = harness.getPermanentId(player1, "Krosan Verge");
        castAvenFogbringer(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Krosan Verge");
        harness.assertInHand(player1, "Krosan Verge");
        harness.assertOnBattlefield(player1, "Aven Fogbringer");
    }

    @Test
    @DisplayName("ETB cannot target a creature")
    void etbRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.setHand(player1, List.of(new AvenFogbringer()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAvenFogbringer(UUID targetId) {
        harness.setHand(player1, List.of(new AvenFogbringer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, List.of(targetId));
    }
}
