package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WortTheRaidmother;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RebornHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target multicolored card from your graveyard to your hand")
    void returnsMulticoloredCardToHand() {
        Card multicolored = new WortTheRaidmother();
        harness.setGraveyard(player1, List.of(multicolored));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, multicolored.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(multicolored.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(multicolored.getId()));
    }

    @Test
    @DisplayName("Cannot target a monocolored card in your graveyard")
    void cannotTargetMonocoloredCard() {
        Card monocolored = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(monocolored));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, monocolored.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card multicolored = new WortTheRaidmother();
        harness.setGraveyard(player2, List.of(multicolored));
        harness.setHand(player1, List.of(new RebornHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, multicolored.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }
}
