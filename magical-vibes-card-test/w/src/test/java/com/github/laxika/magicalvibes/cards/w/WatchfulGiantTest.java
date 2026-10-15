package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatchfulGiant.class})
class WatchfulGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Human token")
    void enteringTheBattlefieldCreatesHumanToken() {
        harness.setHand(player1, List.of(new WatchfulGiant()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
    }

    @Test
    @DisplayName("The Human is created when the enters trigger resolves, not when the Giant spell resolves")
    void tokenCreationWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new WatchfulGiant()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.assertNotOnBattlefield(player1, "Human");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Watchful Giant");
        harness.assertNotOnBattlefield(player1, "Human");

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        harness.assertOnBattlefield(player1, "Human");
        harness.assertNotOnBattlefield(player2, "Human");
    }

    @Test
    @DisplayName("Each Giant entering creates exactly one Human without retriggering from the token")
    void eachGiantCreatesOneToken() {
        harness.setHand(player1, List.of(new WatchfulGiant(), new WatchfulGiant()));
        harness.addMana(player1, ManaColor.WHITE, 12);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> assertThat(token.getCard().getSubtypes())
                        .containsExactly(CardSubtype.HUMAN));
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Human");
    }
}
