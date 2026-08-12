package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CranialArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself, shuffles the target graveyard, and draws a card")
    void exilesSelfShufflesTargetGraveyardAndDraws() {
        harness.addToBattlefield(player1, new CranialArchive());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GiantSpider()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GiantSpider()));
        harness.setHand(player1, List.of());
        int targetLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(targetLibrarySize + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Cranial Archive");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cranial Archive"));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new CranialArchive());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                gd.playerBattlefields.get(player2.getId()).get(0).getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
