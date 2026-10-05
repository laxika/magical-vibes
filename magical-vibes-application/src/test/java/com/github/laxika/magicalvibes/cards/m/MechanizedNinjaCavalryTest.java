package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MechanizedNinjaCavalry.class)
class MechanizedNinjaCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 1/1 colorless Robot artifact creature token")
    void enteringCreatesRobotToken() {
        harness.setHand(player1, List.of(new MechanizedNinjaCavalry()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().getPower()).isEqualTo(1);
        assertThat(robot.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering without being cast creates exactly one token for its controller")
    void enteringWithoutCastingCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new MechanizedNinjaCavalry());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        Permanent robot = findPermanent(player2, "Robot");
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(robot.getCard().getSubtypes())
                .containsExactly(com.github.laxika.magicalvibes.model.CardSubtype.ROBOT);
        assertThat(robot.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enters trigger creates its token even after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent cavalry = harness.enterBattlefieldAndReturn(player1, new MechanizedNinjaCavalry());
        gd.playerBattlefields.get(player1.getId()).remove(cavalry);
        gd.playerGraveyards.get(player1.getId()).add(cavalry.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Robot").getCard().isToken()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
