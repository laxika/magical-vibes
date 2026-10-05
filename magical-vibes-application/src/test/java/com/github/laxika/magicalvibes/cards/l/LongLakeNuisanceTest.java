package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LongLakeNuisance.class, Forest.class})
class LongLakeNuisanceTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, recruit creates a Soldier after discarding a nonland card")
    void entersAndRecruitsAfterNonlandDiscard() {
        prepareNuisance(new LongLakeNuisance(), new Forest());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Long Lake Nuisance");
        harness.assertInGraveyard(player1, "Long Lake Nuisance");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("When it enters, recruit does not create a Soldier after discarding a land")
    void entersAndDoesNotRecruitAfterLandDiscard() {
        prepareNuisance(new Forest(), new LongLakeNuisance());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Long Lake Nuisance");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    void recruitCreatesWhiteHumanSoldier() {
        prepareNuisance(new LongLakeNuisance(), new Forest());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    void recruitCreatesTokenBeforePlayersReceivePriority() {
        prepareNuisance(new LongLakeNuisance(), new Forest());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void emptyHandCanDiscardTheNonlandCardJustDrawn() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LongLakeNuisance()));
        harness.enterBattlefieldAndReturn(player1, new LongLakeNuisance());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Long Lake Nuisance");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private void prepareNuisance(Card discardedCard, Card drawnCard) {
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.enterBattlefieldAndReturn(player1, new LongLakeNuisance());
    }
}
