package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed(ForgottenCave.class)
class ForgottenCaveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ForgottenCave()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tappingProducesRedMana() {
        Permanent land = addCaveReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ForgottenCave()));
        harness.setLibrary(player1, List.of(new ForgottenCave()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Forgotten Cave");
        harness.assertInHand(player1, "Forgotten Cave");
    }

    @Test
    @DisplayName("Cycling requires red mana")
    void cyclingRequiresRedMana() {
        ForgottenCave cave = new ForgottenCave();
        harness.setHand(player1, List.of(cave));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cave);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addCaveReady(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new ForgottenCave());
        land.setSummoningSick(false);
        return land;
    }
}
