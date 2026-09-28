package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShuriVibraniumTechnologist.class, Forest.class})
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
        harness.setLibrary(player1, List.of(new Forest()));
        castShuri();

        harness.handleListChoice(player1, "Draw a card.");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castShuri() {
        harness.setHand(player1, List.of(new ShuriVibraniumTechnologist()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
