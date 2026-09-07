package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Cleanse.class, BlackKnight.class, EliteVanguard.class, GrizzlyBears.class, HowlingMine.class})
class CleanseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all black creatures on both battlefields")
    void destroysAllBlackCreatures() {
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player2, new BlackKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCleanse();

        harness.assertNotOnBattlefield(player1, "Black Knight");
        harness.assertNotOnBattlefield(player2, "Black Knight");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves nonblack creatures and noncreature permanents alone")
    void leavesNonblackCreaturesAndNoncreaturesAlone() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new BlackKnight());

        castCleanse();

        harness.assertOnBattlefield(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Howling Mine");
        harness.assertNotOnBattlefield(player2, "Black Knight");
    }

    private void castCleanse() {
        harness.setHand(player1, List.of(new Cleanse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
