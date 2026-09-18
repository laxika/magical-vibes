package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
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

@CardUsed({AvenFogbringer.class, RiftstonePortal.class, SuntailHawk.class})
class AvenFogbringerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns the targeted land to its owner's hand")
    void etbReturnsTargetedLand() {
        harness.addToBattlefield(player2, new RiftstonePortal());
        UUID targetId = harness.getPermanentId(player2, "Riftstone Portal");
        castAvenFogbringer(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Riftstone Portal");
        harness.assertInHand(player2, "Riftstone Portal");
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

    @Test
    @DisplayName("ETB has no effect when no land is available")
    void etbHasNoEffectWithoutLandTarget() {
        harness.castFromHand(player1, new AvenFogbringer(), "{3}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aven Fogbringer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castAvenFogbringer(UUID targetId) {
        harness.setHand(player1, List.of(new AvenFogbringer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, List.of(targetId));
    }
}
