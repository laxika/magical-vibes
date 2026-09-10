package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SonicBurst;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({Grollub.class, Shock.class, SonicBurst.class, RagingGoblin.class})
class GrollubTest extends BaseCardTest {

    @Test
    @DisplayName("When Grollub is dealt damage, its opponent gains that much life")
    void opponentGainsDamageAmount() {
        harness.addToBattlefield(player1, new Grollub());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID grollubId = harness.getPermanentId(player1, "Grollub");
        harness.castInstant(player2, 0, grollubId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertOnBattlefield(player1, "Grollub");
    }

    @Test
    @DisplayName("When Grollub is dealt lethal damage, its opponent still gains that much life")
    void opponentGainsLifeAfterGrollubDies() {
        harness.addToBattlefield(player1, new Grollub());
        harness.setHand(player2, List.of(new SonicBurst(), new RagingGoblin()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID grollubId = harness.getPermanentId(player1, "Grollub");
        harness.castInstant(player2, 0, grollubId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 24);
        harness.assertNotOnBattlefield(player1, "Grollub");
        harness.assertInGraveyard(player1, "Grollub");
    }

    @Test
    @DisplayName("When Grollub is dealt combat damage, its opponent gains that much life")
    void opponentGainsCombatDamageAmount() {
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player2, new Grollub());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
