package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@CardUsed({ReinforcedRonin.class, GrizzlyBears.class})
class ReinforcedRoninTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand at its controller's end step")
    void returnsSelfAtControllerEndStep() {
        addReadyRonin(player1);

        advanceToEndStep(player1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reinforced Ronin");
        harness.assertInHand(player1, "Reinforced Ronin");
    }

    @Test
    @DisplayName("Channeling Reinforced Ronin draws a card")
    void channelsAndDrawsCard() {
        harness.setHand(player1, List.of(new ReinforcedRonin()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reinforced Ronin");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyRonin(Player player) {
        Permanent ronin = new Permanent(new ReinforcedRonin());
        ronin.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ronin);
        return ronin;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
