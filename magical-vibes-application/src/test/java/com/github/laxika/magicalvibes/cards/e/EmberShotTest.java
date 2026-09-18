package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberShot.class, GiantWarthog.class, RiftstonePortal.class})
class EmberShotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and draws a card")
    void damagesCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Giant Warthog"));

        assertThat(findPermanent(player2, "Giant Warthog").getMarkedDamage()).isEqualTo(3);
        harness.assertInHand(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Deals 3 damage to a player and draws a card")
    void damagesPlayerAndDrawsCard() {
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.addMana(player1, ManaColor.RED, 7);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertInHand(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new RiftstonePortal());
        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Riftstone Portal")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }
}
