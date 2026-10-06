package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FarbogRevenant;
import com.github.laxika.magicalvibes.cards.t.Throttle;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RottenheartGhoul.class, FarbogRevenant.class, Throttle.class, Swamp.class})
class RottenheartGhoulTest extends BaseCardTest {

    private void startMainPhaseWithThrottle() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new Throttle(), new Swamp())));
        harness.addMana(player1, ManaColor.BLACK, 5);
    }

    @Test
    @DisplayName("When this creature dies, the chosen player discards a card")
    void deathTriggerDiscardsFromChosenPlayer() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new RottenheartGhoul());
        harness.setHand(player2, new ArrayList<>(List.of(new FarbogRevenant(), new Swamp())));
        startMainPhaseWithThrottle();

        harness.castAndResolveInstant(player1, 0, ghoul.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The death trigger can target the other player")
    void deathTriggerCanTargetOpponent() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new RottenheartGhoul());
        harness.setHand(player2, new ArrayList<>(List.of(new FarbogRevenant(), new Swamp())));
        startMainPhaseWithThrottle();

        harness.castAndResolveInstant(player1, 0, ghoul.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The death trigger does not happen while this creature remains on the battlefield")
    void noDeathNoDiscard() {
        harness.addToBattlefield(player1, new RottenheartGhoul());
        Permanent revenant = harness.addToBattlefieldAndReturn(player1, new FarbogRevenant());
        harness.setHand(player2, new ArrayList<>(List.of(new FarbogRevenant(), new Swamp())));
        startMainPhaseWithThrottle();

        harness.castAndResolveInstant(player1, 0, revenant.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
    @Test
    @DisplayName("A player with an empty hand is still a legal target")
    void emptyHandTargetResolvesWithoutDiscard() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new RottenheartGhoul());
        harness.setHand(player2, List.of());
        startMainPhaseWithThrottle();

        harness.castAndResolveInstant(player1, 0, ghoul.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rottenheart Ghoul");
    }

    @Test
    @DisplayName("The targeted player chooses which card to discard")
    void targetedPlayerChoosesDiscard() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new RottenheartGhoul());
        FarbogRevenant kept = new FarbogRevenant();
        Swamp discarded = new Swamp();
        harness.setHand(player2, List.of(kept, discarded));
        startMainPhaseWithThrottle();

        harness.castAndResolveInstant(player1, 0, ghoul.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
