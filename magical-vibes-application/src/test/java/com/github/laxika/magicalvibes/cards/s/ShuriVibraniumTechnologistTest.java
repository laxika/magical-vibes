package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShuriVibraniumTechnologist.class})
class ShuriVibraniumTechnologistTest extends BaseCardTest {

    @Test
    void createsFlyingRobotHeroArtifactToken() {
        castShuri();

        harness.handleListChoice(player1,
                "Create a 1/1 colorless Robot Hero artifact creature token with flying.");
        harness.passBothPriorities();

        Permanent robot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, robot, Keyword.FLYING)).isTrue();
    }

    @Test
    void drawsACard() {
        harness.setLibrary(player1, List.of(new ShuriVibraniumTechnologist()));
        castShuri();

        harness.handleListChoice(player1, "Draw a card.");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void tokenHasItsDefaultNameAndBothSubtypesAndIsColorless() {
        castShuri();

        harness.handleListChoice(player1,
                "Create a 1/1 colorless Robot Hero artifact creature token with flying.");
        harness.passBothPriorities();

        Permanent robot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(robot.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ROBOT, CardSubtype.HERO);
        assertThat(robot.getCard().getColors()).isEmpty();
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(robot.getCard().getName()).isEqualTo("Robot Hero Token");
    }

    @Test
    void drawModeResolvesAfterShuriLeavesTheBattlefield() {
        ShuriVibraniumTechnologist libraryCard = new ShuriVibraniumTechnologist();
        harness.setLibrary(player1, List.of(libraryCard));
        castShuri();
        harness.handleListChoice(player1, "Draw a card.");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);

        Permanent shuri = findPermanent(player1, "Shuri, Vibranium Technologist");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, shuri));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shuri, Vibranium Technologist");
    }

    private void castShuri() {
        harness.setHand(player1, List.of(new ShuriVibraniumTechnologist()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
