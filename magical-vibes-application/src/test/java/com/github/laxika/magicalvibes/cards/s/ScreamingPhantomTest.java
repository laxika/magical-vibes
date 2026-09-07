package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamingPhantom.class, Forest.class})
class ScreamingPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills the top card of its controller's library")
    void attackingMillsTopCard() {
        Card milled = new Forest();
        harness.setLibrary(player1, List.of(milled));
        addCreatureReady(player1, new ScreamingPhantom());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
    }
}
