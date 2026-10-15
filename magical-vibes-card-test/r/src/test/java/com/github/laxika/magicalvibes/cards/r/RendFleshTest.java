package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RendFlesh.class, IsamaruHoundOfKonda.class, KamiOfOldStone.class, Swamp.class})
class RendFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Can destroy its controller's non-Spirit creature")
    void destroysOwnNonSpiritCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertInGraveyard(player1, "Isamaru, Hound of Konda");
        harness.assertInGraveyard(player1, "Rend Flesh");
    }

    @Test
    @DisplayName("Does not affect another creature when its target is destroyed in response")
    void targetDestroyedInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new RendFlesh(), new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Rend Flesh"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Destroys a non-Spirit creature")
    void destroysNonSpiritCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
    }

    @Test
    @DisplayName("Cannot target a Spirit")
    void cannotTargetSpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Spirit creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Spirit creature");
    }
}
