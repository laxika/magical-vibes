package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dusk.class, Dawn.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class DuskTest extends BaseCardTest {

    @Test
    @DisplayName("Dusk destroys creatures with power 3 or greater")
    void duskDestroysCreaturesWithPowerAtLeastThree() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Dusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Dawn returns all low-power creature cards to hand, then exiles Dusk")
    void dawnReturnsLowPowerCreaturesAndExilesParentCard() {
        Card lowPowerCreature = new GrizzlyBears();
        Card highPowerCreature = new HillGiant();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(new Dusk(), lowPowerCreature, highPowerCreature, land));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .containsExactly(highPowerCreature, land);
        assertThat(gameData.playerHands.get(player1.getId()))
                .contains(lowPowerCreature);
        assertThat(gameData.playerHands.get(player1.getId()))
                .doesNotContain(highPowerCreature, land);
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dusk"));
    }
}
