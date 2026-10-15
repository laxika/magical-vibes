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
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

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
        UUID islandId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

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
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Wurmcoil Engine");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wurmcoil Engine");
    }

    @Test
    void decliningEtbLeavesTargetOnBattlefield() {
        prepareMainPhase();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BrinelinTheMoonKraken()).getId();
        harness.setHand(player1, List.of(new BrinelinTheMoonKraken()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Brinelin, the Moon Kraken");
        harness.assertNotInHand(player2, "Brinelin, the Moon Kraken");
    }

    @Test
    void lowManaValueSpellDoesNotTrigger() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new BrinelinTheMoonKraken());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentHighManaValueSpellDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new BrinelinTheMoonKraken());
        harness.setHand(player2, List.of(new WurmcoilEngine()));
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Wurmcoil Engine");
    }

    @Test
    void castTriggerCanTargetBrinelinItselfAndDeclineOnResolution() {
        prepareMainPhase();
        UUID brinelinId = harness.addToBattlefieldAndReturn(player1, new BrinelinTheMoonKraken()).getId();
        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(brinelinId);
        harness.handlePermanentChosen(player1, brinelinId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Brinelin, the Moon Kraken");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wurmcoil Engine");
    }
}
