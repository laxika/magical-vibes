package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BakersbaneDuo.class, Shock.class})
class BakersbaneDuoTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodOnEnter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bakersbane Duo");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Gets +1/+1 when its controller expends four")
    void getsBoostWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BakersbaneDuo(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bakersbane Duo").getPowerModifier()).isEqualTo(1);
        assertThat(findPermanent(player1, "Bakersbane Duo").getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the expend boost before four total mana is spent")
    void doesNotGetBoostBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BakersbaneDuo(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bakersbane Duo").getPowerModifier()).isZero();
        assertThat(findPermanent(player1, "Bakersbane Duo").getToughnessModifier()).isZero();
    }
}
