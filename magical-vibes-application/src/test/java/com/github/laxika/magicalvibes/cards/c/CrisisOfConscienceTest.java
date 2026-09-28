package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CrisisOfConscience.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
class CrisisOfConscienceTest extends BaseCardTest {

    @Test
    @DisplayName("The token mode destroys tokens and leaves nontoken permanents alone")
    void destroysAllTokens() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, newToken());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Soldier");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The nonland nontoken mode destroys only matching permanents")
    void destroysNonlandNontokenPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, newToken());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Soldier");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private Card newToken() {
        Card token = new Card();
        token.setName("Soldier");
        token.setType(CardType.CREATURE);
        token.setPower(2);
        token.setToughness(2);
        token.setToken(true);
        return token;
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new CrisisOfConscience()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
