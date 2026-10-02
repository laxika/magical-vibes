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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenFogbringer.class, KrosanVerge.class, SuntailHawk.class})
class AvenFogbringerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns the targeted land to its owner's hand")
    void etbReturnsTargetedLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KrosanVerge()).getId();
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
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new KrosanVerge()).getId();
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SuntailHawk()).getId();
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

    @Test
    @DisplayName("ETB returns a land to its owner rather than its controller")
    void etbReturnsLandToOwner() {
        KrosanVerge land = new KrosanVerge();
        land.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, land).getId();
        castAvenFogbringer(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Krosan Verge");
        harness.assertInHand(player1, "Krosan Verge");
        harness.assertNotInHand(player2, "Krosan Verge");
    }

    @Test
    @DisplayName("ETB does not return a target that left the battlefield before resolution")
    void etbDoesNotReturnDepartedLand() {
        var land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        castAvenFogbringer(land.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerGraveyards.get(player2.getId()).add(land.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Krosan Verge");
        harness.assertNotInHand(player2, "Krosan Verge");
        harness.assertOnBattlefield(player1, "Aven Fogbringer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB has no effect when no land is available")
    void etbHasNoEffectWithoutLandTarget() {
        harness.castFromHand(player1, new AvenFogbringer(), "{3}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aven Fogbringer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
