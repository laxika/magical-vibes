package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CullingSun.class, Gristleback.class, GhorClanSavage.class, GruulSignet.class})
class CullingSunTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures with mana value 3 or less and spares bigger creatures and noncreatures")
    void destroysMatchingCreaturesOnly() {
        harness.addToBattlefield(player1, new Gristleback());
        harness.addToBattlefield(player2, new Gristleback());
        harness.addToBattlefield(player2, new GhorClanSavage());
        harness.addToBattlefield(player2, new GruulSignet());

        harness.setHand(player1, List.of(new CullingSun()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Gristleback");
        harness.assertInGraveyard(player2, "Gristleback");
        harness.assertOnBattlefield(player2, "Ghor-Clan Savage");
        harness.assertOnBattlefield(player2, "Gruul Signet");
        harness.assertInGraveyard(player1, "Culling Sun");
    }
}
