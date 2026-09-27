package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RendSpirit.class, KamiOfOldStone.class, IsamaruHoundOfKonda.class})
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
}
