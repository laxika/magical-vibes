package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.b.BlightsteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hellfire.class, BlackKnight.class, GrizzlyBears.class, HowlingMine.class, BlightsteelColossus.class})
class HellfireTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonblack creatures and deals three damage plus the number destroyed")
    void destroysNonblackCreaturesAndDealsDamageBasedOnDestroyedCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player2, new HowlingMine());

        castHellfire();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Black Knight");
        harness.assertOnBattlefield(player2, "Howling Mine");
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Does not count an indestructible creature in the damage")
    void doesNotCountIndestructibleCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BlightsteelColossus());

        castHellfire();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Blightsteel Colossus");
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    private void castHellfire() {
        harness.setHand(player1, List.of(new Hellfire()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
