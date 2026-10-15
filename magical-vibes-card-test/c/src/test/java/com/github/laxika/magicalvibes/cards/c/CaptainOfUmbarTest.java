package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainOfUmbar.class, GrizzlyBears.class})
class CaptainOfUmbarTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then discards a card")
    void drawsThenDiscards() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainOfUmbar());
        captain.setSummoningSick(false);
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        gd.playerDecks.get(player1.getId()).addFirst(drawn);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(drawn);
        assertThat(captain.isTapped()).isTrue();
    }
}
