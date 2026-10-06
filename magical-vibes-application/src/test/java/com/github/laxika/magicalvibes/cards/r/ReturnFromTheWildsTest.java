package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnFromTheWilds.class, Forest.class})
class ReturnFromTheWildsTest extends BaseCardTest {

    @Test
    void choosesBasicLandAndHuman() {
        cast(0, 1);
        chooseForest();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Human").getCard().isToken()).isTrue();
    }

    @Test
    void choosesBasicLandAndFood() {
        cast(0, 2);
        chooseForest();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().isToken()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void choosesHumanAndFood() {
        cast(1, 2);

        assertThat(findPermanent(player1, "Human").getCard().isToken()).isTrue();
        assertThat(findPermanent(player1, "Food").getCard().isToken()).isTrue();
    }

    @Test
    void failingToFindLandStillCreatesHuman() {
        cast(0, 1);
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryStillCreatesFood() {
        harness.setHand(player1, List.of(new ReturnFromTheWilds()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2}, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Human");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int... modes) {
        harness.setHand(player1, List.of(new ReturnFromTheWilds()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }

    private void chooseForest() {
        harness.handleCardChosen(player1, 0);
    }
}
