package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadeCastSentinel.class, GrizzlyBears.class, HillGiant.class})
class JadeCastSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a card from your graveyard on the bottom of your library")
    void tucksOwnGraveyardCard() {
        int sentinelIndex = addSentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, sentinelIndex, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Puts a card from an opponent's graveyard on the bottom of its owner's library")
    void tucksOpponentGraveyardCard() {
        int sentinelIndex = addSentinel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, sentinelIndex, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(tucked.getId());
    }

    private int addSentinel() {
        Permanent sentinel = addCreatureReady(player1, new JadeCastSentinel());
        return gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
    }
}
