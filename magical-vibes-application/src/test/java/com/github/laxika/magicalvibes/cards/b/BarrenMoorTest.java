package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AccursedCentaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrenMoor.class, AccursedCentaur.class})
class BarrenMoorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new BarrenMoor()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one black mana")
    void tappingProducesBlackMana() {
        Permanent land = addMoorReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BarrenMoor()));
        harness.setLibrary(player1, List.of(new AccursedCentaur()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Barren Moor");
        harness.assertInHand(player1, "Accursed Centaur");
    }

    @Test
    @DisplayName("Cycling requires black mana")
    void cyclingRequiresBlackMana() {
        BarrenMoor moor = new BarrenMoor();
        harness.setHand(player1, List.of(moor));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(moor);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addMoorReady(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new BarrenMoor());
        land.setSummoningSick(false);
        return land;
    }
}
