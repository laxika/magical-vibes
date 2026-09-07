package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheBazaar.class, Forest.class, GrizzlyBears.class})
class MagusOfTheBazaarTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discards three cards")
    void drawsTwoThenDiscardsThree() {
        Permanent magus = addReadyMagus();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(magus.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private Permanent addReadyMagus() {
        Card magusCard = new MagusOfTheBazaar();
        Permanent magus = new Permanent(magusCard);
        magus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(magus);
        return magus;
    }
}
