package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalakirFamiliar.class, AngelOfMercy.class})
class MalakirFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when its controller gains life")
    void getsBoostOnControllerLifeGain() {
        Permanent familiar = addCreatureReady(player1, new MalakirFamiliar());
        castAngelOfMercy(player1);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void noBoostOnOpponentLifeGain() {
        Permanent familiar = addCreatureReady(player1, new MalakirFamiliar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castAngelOfMercy(player2);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost lasts until end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent familiar = addCreatureReady(player1, new MalakirFamiliar());
        castAngelOfMercy(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(1);
    }

    private void castAngelOfMercy(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new AngelOfMercy()));
        harness.addMana(player, ManaColor.WHITE, 5);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
