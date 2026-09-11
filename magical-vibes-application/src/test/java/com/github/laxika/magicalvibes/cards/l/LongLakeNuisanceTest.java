package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LongLakeNuisance.class, Forest.class, GrizzlyBears.class})
class LongLakeNuisanceTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, recruit creates a Soldier after discarding a nonland card")
    void entersAndRecruitsAfterNonlandDiscard() {
        Permanent nuisance = prepareNuisance(new GrizzlyBears(), new Forest());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Long Lake Nuisance");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("When it enters, recruit does not create a Soldier after discarding a land")
    void entersAndDoesNotRecruitAfterLandDiscard() {
        prepareNuisance(new Forest(), new GrizzlyBears());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private Permanent prepareNuisance(Card discardedCard, Card drawnCard) {
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        return harness.enterBattlefieldAndReturn(player1, new LongLakeNuisance());
    }
}
