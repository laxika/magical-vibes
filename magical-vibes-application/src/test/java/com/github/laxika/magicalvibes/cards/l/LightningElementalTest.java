package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningElemental.class, ViridianLongbow.class})
class LightningElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately after entering because of haste")
    void canAttackImmediatelyBecauseOfHaste() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LightningElemental());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Can activate a tap ability immediately after entering because of haste")
    void canActivateTapAbilityImmediatelyBecauseOfHaste() {
        harness.setLife(player2, 20);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new LightningElemental());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(elemental.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(elemental.isTapped()).isTrue();
    }
}
