package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaPhoenix;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshesOfTheAbhorrent.class, GrizzlyBears.class, MagmaPhoenix.class, Shock.class, ThinkTwice.class})
class AshesOfTheAbhorrentTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 1 life when an opponent's creature dies")
    void gainsLifeWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve life gain trigger

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller gains 1 life when own creature dies")
    void gainsLifeWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill own creature with opponent's Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve life gain trigger

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Prevents flashback casting when Ashes is on the battlefield")
    void preventsFlashbackCasting() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        // Player 2 should not be able to cast flashback
        setupPlayer2Active();
        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents controller from casting flashback too")
    void preventsControllerFlashbackCasting() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Controller also can't cast flashback (it affects all players)
        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback indices are empty when Ashes is on the battlefield")
    void flashbackIndicesAreEmpty() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        List<Integer> playable = harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player2.getId());
        assertThat(playable).isEmpty();
    }

    @Test
    @DisplayName("Prevents graveyard activated abilities when Ashes is on the battlefield")
    void preventsGraveyardAbilityActivation() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player2, List.of(new MagmaPhoenix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        setupPlayer2Active();
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents controller from activating graveyard abilities too")
    void preventsControllerGraveyardAbilityActivation() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player1, List.of(new MagmaPhoenix()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each Ashes triggers independently for a creature death")
    void multipleCopiesEachGainLife() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ashes allows death triggers and gains life for each resulting creature death")
    void deathTriggersStillResolve() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MagmaPhoenix());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID phoenixId = harness.getPermanentId(player2, "Magma Phoenix");

        harness.castAndResolveInstant(player1, 0, phoenixId);
        harness.castAndResolveInstant(player1, 0, phoenixId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Magma Phoenix");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Ashes does not stop a graveyard ability already on the stack")
    void previouslyActivatedGraveyardAbilityStillResolves() {
        harness.setGraveyard(player1, List.of(new MagmaPhoenix()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.addToBattlefield(player2, new AshesOfTheAbhorrent());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Magma Phoenix");
        harness.assertNotInGraveyard(player1, "Magma Phoenix");
    }

    @Test
    @DisplayName("Ashes does not stop a flashback spell already on the stack")
    void previouslyCastFlashbackStillResolves() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.addToBattlefield(player2, new AshesOfTheAbhorrent());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Think Twice");
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
