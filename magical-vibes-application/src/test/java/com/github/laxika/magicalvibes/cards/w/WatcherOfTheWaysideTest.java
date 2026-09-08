package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatcherOfTheWayside.class, Forest.class, Island.class})
class WatcherOfTheWaysideTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills two cards from the target player and gains 2 life")
    void etbMillsTargetPlayerAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new WatcherOfTheWayside())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void etbCanTargetController() {
        Card top = new Forest();
        Card second = new Island();
        harness.setLife(player1, 15);
        harness.setLibrary(player1, new ArrayList<>(List.of(top, second)));
        harness.setHand(player1, new ArrayList<>(List.of(new WatcherOfTheWayside())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }
}
