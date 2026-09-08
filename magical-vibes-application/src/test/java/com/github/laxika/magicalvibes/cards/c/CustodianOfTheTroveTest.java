package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CustodianOfTheTrove.class)
class CustodianOfTheTroveTest extends BaseCardTest {

    @Test
    @DisplayName("Custodian of the Trove enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CustodianOfTheTrove()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Custodian of the Trove");
        Permanent custodian = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(custodian.isTapped()).isTrue();
    }
}
