package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Obliterate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Absorb.class, AncientKavu.class, Obliterate.class})
class AbsorbTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and Absorb's controller gains 3 life")
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
        harness.castAndResolveInstant(player2, 0, kavu.getId());

        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertInGraveyard(player2, "Absorb");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Gains life even when the targeted spell cannot be countered")
    void gainsLifeWhenTargetCannotBeCountered() {
        harness.setLife(player2, 15);
        harness.addToBattlefield(player1, new AncientKavu());
        Obliterate obliterate = new Obliterate();
        harness.setHand(player1, List.of(obliterate));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.setHand(player2, List.of(new Absorb()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, obliterate.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Absorb");
        harness.assertNotInGraveyard(player1, "Obliterate");
        harness.assertOnBattlefield(player1, "Ancient Kavu");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Obliterate");
        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
    }

    @Test
    @DisplayName("Does not gain life when its target leaves the stack before resolution")
    void doesNotGainLifeWhenTargetLeavesStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);
        AncientKavu kavu = new AncientKavu();
        harness.setHand(player1, List.of(kavu, new Absorb()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Absorb()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, kavu.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, kavu.getId());

        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 15);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Absorb");
        harness.assertInGraveyard(player2, "Absorb");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 15);
    }
}
