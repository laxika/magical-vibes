package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfMagesDoom.class, Shock.class})
class HagOfMagesDoomTest extends BaseCardTest {

    @Test
    void wardCountersOpposingSpellWhenLifeIsNotPaid() {
        Permanent hag = addCreatureReady(player1, new HagOfMagesDoom());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hag.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Hag of Mage's Doom");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    void wardAllowsOpposingSpellWhenTwoLifeIsPaid() {
        Permanent hag = addCreatureReady(player1, new HagOfMagesDoom());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hag.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hag of Mage's Doom");
        harness.assertLife(player2, 18);
    }
}
