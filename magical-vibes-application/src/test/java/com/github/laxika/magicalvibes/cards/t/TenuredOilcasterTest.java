package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenuredOilcaster.class, GrizzlyBears.class, Spellbook.class})
class TenuredOilcasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 when an opponent has eight cards in their graveyard")
    void getsBoostAtEightOpponentGraveyardCards() {
        Permanent oilcaster = harness.addToBattlefieldAndReturn(player1, new TenuredOilcaster());
        int basePower = gqs.getEffectivePower(gd, oilcaster);
        int baseToughness = gqs.getEffectiveToughness(gd, oilcaster);

        fillGraveyard(player2, 7);
        assertThat(gqs.getEffectivePower(gd, oilcaster)).isEqualTo(basePower);

        fillGraveyard(player2, 8);

        assertThat(gqs.getEffectivePower(gd, oilcaster)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, oilcaster)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not get the boost from its controller's graveyard")
    void ownGraveyardDoesNotCount() {
        Permanent oilcaster = harness.addToBattlefieldAndReturn(player1, new TenuredOilcaster());
        int basePower = gqs.getEffectivePower(gd, oilcaster);
        int baseToughness = gqs.getEffectiveToughness(gd, oilcaster);

        fillGraveyard(player1, 8);

        assertThat(gqs.getEffectivePower(gd, oilcaster)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, oilcaster)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Attacking makes each player mill a card")
    void attackTriggerMillsOneCardEach() {
        addCreatureReady(player1, new TenuredOilcaster());
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore - 1);
    }

    @Test
    @DisplayName("Blocking makes each player mill a card")
    void blockTriggerMillsOneCardEach() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new TenuredOilcaster());
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore - 1);
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }
}
