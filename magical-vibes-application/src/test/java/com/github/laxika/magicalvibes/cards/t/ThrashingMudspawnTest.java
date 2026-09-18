package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.PinpointAvalanche;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({ThrashingMudspawn.class, Shock.class, PinpointAvalanche.class, GlorySeeker.class})
class ThrashingMudspawnTest extends BaseCardTest {

    @Test
    @DisplayName("When Thrashing Mudspawn is dealt spell damage, its controller loses that much life")
    void spellDamageCausesMatchingLifeLoss() {
        harness.addToBattlefield(player1, new ThrashingMudspawn());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID mudspawnId = harness.getPermanentId(player1, "Thrashing Mudspawn");
        harness.castInstant(player2, 0, mudspawnId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Thrashing Mudspawn");
    }

    @Test
    @DisplayName("When Thrashing Mudspawn is dealt combat damage, its controller loses that much life")
    void combatDamageCausesMatchingLifeLoss() {
        addCreatureReady(player1, new ThrashingMudspawn());
        addCreatureReady(player2, new GlorySeeker());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Thrashing Mudspawn");
    }

    @Test
    @DisplayName("Lethal damage still makes Thrashing Mudspawn's controller lose that much life")
    void lethalDamageStillMakesControllerLoseLife() {
        harness.addToBattlefield(player2, new ThrashingMudspawn());
        harness.setHand(player1, List.of(new PinpointAvalanche()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID mudspawnId = harness.getPermanentId(player2, "Thrashing Mudspawn");
        harness.castInstant(player1, 0, mudspawnId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Thrashing Mudspawn");
    }
}
