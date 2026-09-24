package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrinelinTheMoonKraken.class, GrizzlyBears.class, Island.class, WurmcoilEngine.class})
class BrinelinTheMoonKrakenTest extends BaseCardTest {

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("When Brinelin enters, accepting the may ability bounces a nonland permanent")
    void etbMayBouncesNonlandPermanent() {
        prepareMainPhase();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new BrinelinTheMoonKraken()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The may ability cannot target a land")
    void onlyNonlandPermanentsAreLegalTargets() {
        prepareMainPhase();
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID islandId = harness.getPermanentId(player2, "Island");

        harness.setHand(player1, List.of(new BrinelinTheMoonKraken()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bearsId).doesNotContain(islandId);
    }

    @Test
    @DisplayName("Casting a spell with mana value 6 or greater triggers Brinelin")
    void highManaValueSpellTriggersBounce() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new BrinelinTheMoonKraken());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }
}
