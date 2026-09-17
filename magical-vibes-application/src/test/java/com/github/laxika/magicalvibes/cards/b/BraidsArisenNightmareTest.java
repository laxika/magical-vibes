package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidsArisenNightmare.class, GrizzlyBears.class, LotusPetal.class, Shock.class})
class BraidsArisenNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent may sacrifice a permanent sharing a type with the sacrificed permanent")
    void opponentSacrificesMatchingPermanent() {
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        harness.addToBattlefield(player2, new LotusPetal());
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Lotus Petal");
        harness.assertInGraveyard(player2, "Lotus Petal");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent who declines causes Braids's controller to draw")
    void opponentDeclinesAndControllerDraws() {
        Shock drawn = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        harness.addToBattlefield(player2, new LotusPetal());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertOnBattlefield(player2, "Lotus Petal");
    }

    @Test
    @DisplayName("An opponent without a matching type causes Braids's controller to draw")
    void noMatchingPermanentDraws() {
        Shock drawn = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the initial sacrifice does nothing")
    void declinesInitialSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        harness.addToBattlefield(player2, new LotusPetal());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void resolveTrigger() {
        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
