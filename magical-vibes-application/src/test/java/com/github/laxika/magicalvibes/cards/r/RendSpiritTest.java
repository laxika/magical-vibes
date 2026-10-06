package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendSpirit.class, KamiOfOldStone.class, IsamaruHoundOfKonda.class, ConsumingVortex.class})
class RendSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Spirit")
    void destroysTargetSpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, spirit.getId());

        harness.assertNotOnBattlefield(player2, "Kami of Old Stone");
        harness.assertInGraveyard(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Cannot target a non-Spirit creature")
    void cannotTargetNonSpirit() {
        Permanent nonSpirit = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonSpirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Spirit");
    }

    @Test
    @DisplayName("Can destroy a Spirit controlled by the caster")
    void destroysOwnSpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, spirit.getId());

        harness.assertNotOnBattlefield(player1, "Kami of Old Stone");
        harness.assertInGraveyard(player1, "Kami of Old Stone");
        harness.assertInGraveyard(player1, "Rend Spirit");
    }

    @Test
    @DisplayName("Does not destroy a Spirit returned to hand in response")
    void targetReturnedToHandBeforeResolution() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, spirit.getId());
        harness.castAndResolveInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Kami of Old Stone");
        harness.assertNotInGraveyard(player2, "Kami of Old Stone");
        harness.assertNotOnBattlefield(player2, "Kami of Old Stone");
        harness.assertInGraveyard(player1, "Rend Spirit");
    }
}
