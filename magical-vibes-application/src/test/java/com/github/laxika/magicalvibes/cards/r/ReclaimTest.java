package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReclaimTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts targeted card from own graveyard on top of own library")
    void resolvePutsCardOnTopOfOwnLibrary() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card opponentsCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentsCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getId().equals(opponentsCard.getId()));
    }

    @Test
    @DisplayName("Fizzles if targeted card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
