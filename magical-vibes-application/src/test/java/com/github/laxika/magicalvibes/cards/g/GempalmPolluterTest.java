package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.w.WitheredWretch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GempalmPolluter.class, WitheredWretch.class, FugitiveWizard.class})
class GempalmPolluterTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling may make a target player lose life for all battlefield Zombies and draws")
    void cyclingMayCauseLifeLossEqualToAllBattlefieldZombies() {
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.addToBattlefield(player2, new WitheredWretch());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setLife(player2, 20);
        prepareCycle();

        cycleAndChoose(player2, true);

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Gempalm Polluter");
        harness.assertInHand(player1, "Withered Wretch");
    }

    @Test
    @DisplayName("Cycling may be declined")
    void cyclingMayBeDeclined() {
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setLife(player2, 20);
        prepareCycle();

        cycleAndChoose(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Gempalm Polluter");
        harness.assertInHand(player1, "Withered Wretch");
    }

    @Test
    @DisplayName("Cycling with no Zombies can still target a player")
    void cyclingWithNoZombiesStillTargetsAPlayer() {
        harness.setLife(player2, 20);
        prepareCycle();

        cycleAndChoose(player2, true);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Gempalm Polluter");
        harness.assertInHand(player1, "Withered Wretch");
    }

    @Test
    @DisplayName("Cycling can target the cycling player's own life total")
    void cyclingCanTargetController() {
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setLife(player1, 20);
        prepareCycle();

        cycleAndChoose(player1, true);

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Gempalm Polluter");
        harness.assertInHand(player1, "Withered Wretch");
    }

    private void prepareCycle() {
        harness.setHand(player1, List.of(new GempalmPolluter()));
        harness.setLibrary(player1, List.of(new WitheredWretch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void cycleAndChoose(Player targetPlayer, boolean accept) {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetPlayer.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
        harness.passBothPriorities();
    }
}
