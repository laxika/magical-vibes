package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Absorb.class, AncientKavu.class})
class AbsorbTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and its caster gains 3 life")
    void countersSpellAndGains3Life() {
        harness.setLife(player2, 15);

        AncientKavu kavu = new AncientKavu();
        harness.setHand(player1, List.of(kavu));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new Absorb()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, kavu.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertInGraveyard(player2, "Absorb");
        harness.assertLife(player2, 18);
    }
}
