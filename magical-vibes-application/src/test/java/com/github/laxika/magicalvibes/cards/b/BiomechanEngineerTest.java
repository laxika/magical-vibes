package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiomechanEngineer.class, Forest.class, Island.class})
class BiomechanEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a Lander token")
    void enteringCreatesLander() {
        harness.setHand(player1, List.of(new BiomechanEngineer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability draws two cards and creates a Robot")
    void activatedAbilityDrawsAndCreatesRobot() {
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new BiomechanEngineer());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(engineer), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw);
        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().getPower()).isEqualTo(2);
        assertThat(robot.getCard().getToughness()).isEqualTo(2);
    }
}
