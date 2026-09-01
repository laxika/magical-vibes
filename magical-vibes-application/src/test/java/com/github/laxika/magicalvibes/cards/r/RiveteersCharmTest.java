package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiveteersCharm.class, GrizzlyBears.class, HillGiant.class})
class RiveteersCharmTest extends BaseCardTest {

    @Test
    @DisplayName("The sacrifice mode sacrifices only a creature or planeswalker with greatest mana value")
    void sacrificesGreatestManaValuePermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        cast(0, player2.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The top-card mode exiles three cards and allows one to be played from exile")
    void exilesTopThreeAndAllowsPlayingThem() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        cast(1, null);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, third);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The graveyard mode exiles any target player's graveyard")
    void exilesTargetPlayersGraveyard() {
        GrizzlyBears card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        cast(2, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The sacrifice mode cannot target its controller")
    void sacrificeModeRequiresAnOpponent() {
        harness.setHand(player1, List.of(new RiveteersCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new RiveteersCharm()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
