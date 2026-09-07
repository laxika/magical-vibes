package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkbladeBruiser.class, Shock.class})
class JunkbladeBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+1 when its controller expends four")
    void getsBoostWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        Permanent bruiser = findPermanent(player1, "Junkblade Bruiser");
        assertThat(bruiser.getPowerModifier()).isEqualTo(2);
        assertThat(bruiser.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the expend boost before four total mana is spent")
    void doesNotGetBoostBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        Permanent bruiser = findPermanent(player1, "Junkblade Bruiser");
        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The expend boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }

        Permanent bruiser = findPermanent(player1, "Junkblade Bruiser");
        assertThat(bruiser.getPowerModifier()).isEqualTo(2);
        assertThat(bruiser.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();
    }
}
