package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.s.SnuffOut;
import com.github.laxika.magicalvibes.cards.w.WaveOfReckoning;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({MoonlitWake.class, FreshVolunteers.class, SnuffOut.class, WaveOfReckoning.class})
class MoonlitWakeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 1 life when an opponent's creature dies")
    void gainsLifeWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new MoonlitWake());
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new SnuffOut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID creatureId = harness.getPermanentId(player2, "Fresh Volunteers");
        harness.castAndResolveInstant(player1, 0, creatureId);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller gains 1 life when their creature dies")
    void gainsLifeWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new MoonlitWake());
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SnuffOut()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        UUID creatureId = harness.getPermanentId(player1, "Fresh Volunteers");
        harness.castAndResolveInstant(player2, 0, creatureId);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller gains 1 life for each creature that dies simultaneously")
    void gainsLifeForEachCreatureThatDies() {
        harness.addToBattlefield(player1, new MoonlitWake());
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new WaveOfReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fresh Volunteers");
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertLife(player1, 22);
    }
}
