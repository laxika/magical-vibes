package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EndriderSpikespitterTest extends BaseCardTest {

    @Test
    void atMaxSpeedExilesTopCardWithPlayPermission() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    void belowMaxSpeedDoesNotExileTopCard() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }
}
