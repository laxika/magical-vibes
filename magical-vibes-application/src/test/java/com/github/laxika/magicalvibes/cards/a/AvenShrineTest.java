package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WordsOfWisdom;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AvenShrine.class, AvenArcher.class, AvenFlock.class, WordsOfWisdom.class})
class AvenShrineTest extends BaseCardTest {

    @Test
    @DisplayName("The Shrine controller also gains life when casting a matching spell")
    void shrineControllerGainsLife() {
        harness.addToBattlefield(player1, new AvenShrine());
        harness.setGraveyard(player2, List.of(new AvenArcher()));

        harness.castFromHand(player1, new AvenArcher(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aven Archer");
    }

    @Test
    @DisplayName("Each Shrine independently grants life to the spell's caster")
    void multipleShrinesEachTrigger() {
        harness.addToBattlefield(player1, new AvenShrine());
        harness.addToBattlefield(player2, new AvenShrine());
        harness.setGraveyard(player1, List.of(new AvenArcher()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AvenArcher(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.assertLife(player2, 21);
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Aven Archer");
    }

    @Test
    @DisplayName("A Shrine does not trigger for its own casting")
    void shrineDoesNotTriggerForItsOwnCasting() {
        harness.setGraveyard(player1, List.of(new AvenShrine()));

        harness.castFromHand(player1, new AvenShrine(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Aven Shrine");
    }

    @Test
    @DisplayName("The spell's caster gains life for same-name cards in all graveyards")
    void casterGainsLifeForSameNameCardsInAllGraveyards() {
        harness.addToBattlefield(player1, new AvenShrine());
        harness.setGraveyard(player1, List.of(new AvenArcher()));
        harness.setGraveyard(player2, List.of(new AvenArcher(), new AvenFlock()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AvenArcher(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Aven Archer");
    }

    @Test
    @DisplayName("The life amount is counted as the trigger resolves")
    void lifeAmountIsCountedAsTriggerResolves() {
        harness.addToBattlefield(player1, new AvenShrine());
        harness.setGraveyard(player1, List.of(new AvenArcher()));
        harness.setGraveyard(player2, List.of(new AvenArcher(), new AvenFlock()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AvenArcher(), "{3}{W}{W}");
        harness.setGraveyard(player2, List.of(new AvenArcher(), new AvenFlock(), new AvenArcher()));
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Aven Archer");
    }

    @Test
    @DisplayName("A spell with no same-name graveyard cards gains no life")
    void noSameNameCardsMeansNoLifeGain() {
        harness.addToBattlefield(player1, new AvenShrine());
        harness.setGraveyard(player1, List.of(new AvenFlock()));
        harness.setGraveyard(player2, List.of(new AvenFlock()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AvenArcher(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Aven Archer");
    }

    @Test
    @DisplayName("A noncreature spell also triggers life gain for its caster")
    void noncreatureSpellAlsoTriggersLifeGain() {
        harness.addToBattlefield(player1, new AvenShrine());
        harness.setGraveyard(player1, List.of(new WordsOfWisdom()));
        harness.setGraveyard(player2, List.of(new WordsOfWisdom()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new WordsOfWisdom(), "{1}{U}");
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
    }
}
