package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.w.WordsOfWisdom;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NantukoShrine.class, AngelicWall.class, DuskImp.class, WordsOfWisdom.class})
class NantukoShrineTest extends BaseCardTest {

    @Test
    @DisplayName("The spell's caster creates Squirrels for matching cards in all graveyards")
    void casterCreatesSquirrelsForSameNameCardsInAllGraveyards() {
        harness.addToBattlefield(player1, new NantukoShrine());
        harness.setGraveyard(player1, List.of(new AngelicWall()));
        harness.setGraveyard(player2, List.of(new AngelicWall(), new DuskImp()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AngelicWall(), "{1}{W}");
        harness.passBothPriorities();

        List<Permanent> squirrels = findPermanents(player2, "Squirrel");
        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(squirrel.getCard().getPower()).isEqualTo(1);
            assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getSubtypes()).containsExactly(CardSubtype.SQUIRREL);
            assertThat(squirrel.getCard().isToken()).isTrue();
        });
        assertThat(countPermanents(player1, "Squirrel")).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Angelic Wall");
    }

    @Test
    @DisplayName("Nonmatching graveyard cards do not create Squirrels")
    void nonmatchingCardsDoNotCreateSquirrels() {
        harness.addToBattlefield(player1, new NantukoShrine());
        harness.setGraveyard(player1, List.of(new DuskImp()));
        harness.setGraveyard(player2, List.of(new DuskImp()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AngelicWall(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Squirrel")).isZero();
        assertThat(countPermanents(player1, "Squirrel")).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Angelic Wall");
    }

    @Test
    @DisplayName("The matching graveyard count is evaluated when the trigger resolves")
    void countsMatchingCardsAtResolution() {
        harness.addToBattlefield(player1, new NantukoShrine());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AngelicWall(), "{1}{W}");
        harness.setGraveyard(player1, List.of(new AngelicWall()));
        harness.setGraveyard(player2, List.of(new AngelicWall(), new DuskImp(), new AngelicWall()));
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Squirrel")).isEqualTo(3);
        assertThat(countPermanents(player1, "Squirrel")).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Angelic Wall");
    }

    @Test
    @DisplayName("A noncreature spell also creates Squirrels for its caster")
    void noncreatureSpellAlsoCreatesSquirrels() {
        harness.addToBattlefield(player1, new NantukoShrine());
        harness.setGraveyard(player1, List.of(new WordsOfWisdom()));
        harness.setGraveyard(player2, List.of(new WordsOfWisdom(), new DuskImp()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new WordsOfWisdom(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Squirrel")).isEqualTo(2);
        assertThat(countPermanents(player1, "Squirrel")).isZero();

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Words of Wisdom");
    }
}
