package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkSphere.class, Shock.class})
class DarkSphereTest extends BaseCardTest {

    @Test
    void preventsHalfOfTheNextDamageFromChosenSource() {
        harness.setLife(player2, 20);
        Permanent sphere = harness.addToBattlefieldAndReturn(player2, new DarkSphere());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sphere), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Dark Sphere");
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }
}
