package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AnabaBodyguard.class, Roterothopter.class})
class AnabaBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Anaba Bodyguard survive a lethal blocker")
    void firstStrikeDealsDamageBeforeBlocker() {
        addCreatureReady(player1, new AnabaBodyguard());
        Roterothopter blockerCard = new Roterothopter();
        blockerCard.setPower(3);
        blockerCard.setToughness(1);
        addCreatureReady(player2, blockerCard);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Anaba Bodyguard");
        harness.assertInGraveyard(player2, "Roterothopter");
    }
}
