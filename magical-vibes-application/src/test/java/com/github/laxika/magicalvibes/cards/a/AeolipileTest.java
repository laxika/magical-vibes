package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aeolipile.class, IcatianInfantry.class})
class AeolipileTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndDealsTwoDamageToTargetPlayer() {
        harness.addToBattlefield(player1, new Aeolipile());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Aeolipile");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void sacrificesItselfAndDealsTwoDamageToTargetCreature() {
        harness.addToBattlefield(player1, new Aeolipile());
        harness.addToBattlefield(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Icatian Infantry");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aeolipile");
        harness.assertInGraveyard(player2, "Icatian Infantry");
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        harness.addToBattlefield(player1, new Aeolipile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent aeolipile = findPermanent(player1, "Aeolipile");
        aeolipile.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
