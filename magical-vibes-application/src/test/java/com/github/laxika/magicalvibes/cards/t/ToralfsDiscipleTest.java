package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToralfsDisciple.class, LightningBolt.class})
class ToralfsDiscipleTest extends BaseCardTest {

    @Test
    void attackingConjuresFourLightningBoltsIntoTheLibraryAndShuffles() {
        addCreatureReady(player1, new ToralfsDisciple());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(4)
                .extracting(Card::getName)
                .containsOnly("Lightning Bolt");
        assertThat(gameLogContains("conjures 4 cards named Lightning Bolt into"))
                .isTrue();
    }
}
