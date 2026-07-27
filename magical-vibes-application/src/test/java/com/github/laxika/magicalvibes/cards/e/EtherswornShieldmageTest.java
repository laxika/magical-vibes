package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class EtherswornShieldmageTest extends BaseCardTest {

    private void castShieldmage() {
        harness.setHand(player1, List.of(new EtherswornShieldmage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private void shock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prevents all damage to an artifact creature this turn")
    void protectsArtifactCreature() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShieldmage();
        // Shock's 2 damage would be lethal to the 0/2 artifact creature, but it's prevented.
        shock(ornithopter);

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Does not prevent damage to a non-artifact creature")
    void doesNotProtectNonArtifactCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castShieldmage();
        shock(bears);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevention wears off after turn cleanup")
    void wearsOff() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castShieldmage();
        // Simulate end-of-turn cleanup clearing the one-turn prevention.
        gd.allDamagePreventionPredicates.clear();

        shock(ornithopter);

        harness.assertInGraveyard(player1, "Ornithopter");
    }
}
