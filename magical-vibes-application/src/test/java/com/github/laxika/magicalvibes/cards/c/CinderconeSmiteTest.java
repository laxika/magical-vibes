package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderconeSmite.class, HillGiant.class})
class CinderconeSmiteTest extends BaseCardTest {

    @Test
    void startingPlayerDealsDamageWithoutCreatingTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void nonStartingPlayerDealsDamageAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast(player2, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    private void cast(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new CinderconeSmite()));
        harness.addMana(caster, ManaColor.RED, 1);

        harness.castSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
