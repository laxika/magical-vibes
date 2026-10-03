package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.cards.g.GiantSolifuge;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.s.SilhanaLedgewalker;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({CullingSun.class, Gristleback.class, GhorClanSavage.class, GruulSignet.class,
        GiantSolifuge.class, SilhanaLedgewalker.class})
class CullingSunTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures with mana value 3 or less and spares bigger creatures and noncreatures")
    void destroysMatchingCreaturesOnly() {
        harness.addToBattlefield(player1, new Gristleback());
        harness.addToBattlefield(player2, new Gristleback());
        harness.addToBattlefield(player2, new GhorClanSavage());
        harness.addToBattlefield(player2, new GruulSignet());

        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gristleback");
        harness.assertInGraveyard(player2, "Gristleback");
        harness.assertOnBattlefield(player2, "Ghor-Clan Savage");
        harness.assertOnBattlefield(player2, "Gruul Signet");
        harness.assertInGraveyard(player1, "Culling Sun");
    }

    @Test
    @DisplayName("Destroys an opposing hexproof creature but spares a creature with mana value four")
    void destroysHexproofCreatureAndSparesManaValueFour() {
        harness.addToBattlefield(player2, new SilhanaLedgewalker());
        harness.addToBattlefield(player2, new GiantSolifuge());

        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Silhana Ledgewalker");
        harness.assertNotOnBattlefield(player2, "Silhana Ledgewalker");
        harness.assertOnBattlefield(player2, "Giant Solifuge");
        harness.assertNotInGraveyard(player2, "Giant Solifuge");
        harness.assertInGraveyard(player1, "Culling Sun");
    }

    @Test
    @DisplayName("Resolves on an empty battlefield without needing a target")
    void resolvesOnEmptyBattlefield() {
        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Culling Sun");
    }
}
