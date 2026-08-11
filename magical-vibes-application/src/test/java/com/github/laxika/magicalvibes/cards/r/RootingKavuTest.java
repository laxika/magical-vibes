package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger exiles Rooting Kavu and shuffles creature cards into the library")
    void acceptingDeathTriggerExilesAndShufflesCreatureCards() {
        harness.addToBattlefield(player1, new RootingKavu());
        Card rootingKavu = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card bears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock, hillGiant));

        destroyRootingKavu();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(rootingKavu.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bears.getId(), hillGiant.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(shock.getId())
                .doesNotContain(bears.getId(), hillGiant.getId(), rootingKavu.getId());
    }

    @Test
    @DisplayName("Declining the death trigger leaves Rooting Kavu and the graveyard cards in the graveyard")
    void decliningDeathTriggerDoesNothing() {
        harness.addToBattlefield(player1, new RootingKavu());
        Card rootingKavu = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));

        destroyRootingKavu();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(rootingKavu.getId(), bears.getId(), shock.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(bears.getId(), rootingKavu.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(rootingKavu.getId());
    }

    private void destroyRootingKavu() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
