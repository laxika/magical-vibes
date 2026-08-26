package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShamansTrance.class, DarkRitual.class, Mountain.class})
class ShamansTranceTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a spell from an opponent's graveyard and returns it to its owner's graveyard")
    void castsSpellFromOpponentsGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(trance));
        harness.setGraveyard(player2, List.of(ritual));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromGraveyard(player1, ritual.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ritual);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(trance);
    }

    @Test
    @DisplayName("Allows playing a land from an opponent's graveyard")
    void playsLandFromOpponentsGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(trance));
        harness.setGraveyard(player2, List.of(mountain));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, mountain.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Prevents other players from playing cards from their graveyards")
    void preventsOtherPlayersFromPlayingFromTheirGraveyards() {
        ShamansTrance trance = new ShamansTrance();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(trance));
        harness.setGraveyard(player2, List.of(ritual));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        prepareMainPhase(player2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, ritual.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
