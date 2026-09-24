package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KindredSummons.class, AvianChangeling.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class KindredSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts as many chosen-type creatures onto the battlefield as creatures of that type you control")
    void putsMatchingCreaturesOntoBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card nonmatching = new Forest();
        Card firstMatching = new GrizzlyBears();
        Card secondNonmatching = new HillGiant();
        Card secondMatching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatching, firstMatching, secondNonmatching, secondMatching));

        castKindredSummons();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstMatching.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(secondMatching.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonmatching, secondNonmatching);
    }

    @Test
    @DisplayName("Changeling creatures count toward and match the chosen type")
    void changelingCountsAndMatches() {
        harness.addToBattlefield(player1, new AvianChangeling());
        Card nonmatching = new Forest();
        Card matchingChangeling = new AvianChangeling();
        harness.setLibrary(player1, List.of(nonmatching, matchingChangeling));

        castKindredSummons();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(matchingChangeling.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    @DisplayName("Only creatures of the chosen type you control determine the count")
    void onlyChosenTypeYouControlCounts() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        Card matching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(matching));

        castKindredSummons();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(matching.getId()));
    }

    private void castKindredSummons() {
        harness.setHand(player1, List.of(new KindredSummons()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
