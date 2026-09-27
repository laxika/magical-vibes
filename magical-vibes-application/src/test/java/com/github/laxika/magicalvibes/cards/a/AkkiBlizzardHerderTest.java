package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiBlizzardHerder.class, GnarledMass.class, GodsEyeGateToTheReikai.class,
        TendoIceBridge.class})
class AkkiBlizzardHerderTest extends BaseCardTest {

    private void setupCombatWhereHerderDies() {
        Permanent perm = findPermanent(player1, "Akki Blizzard-Herder");
        perm.setSummoningSick(false);
        perm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GnarledMass());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When it dies, each player with exactly one land loses it")
    void eachPlayerWithOneLandLosesIt() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player2, new TendoIceBridge());

        setupCombatWhereHerderDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tendo Ice Bridge");
        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
    }

    @Test
    @DisplayName("A player with multiple lands chooses which one to sacrifice")
    void playerWithMultipleLandsChooses() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.addToBattlefield(player2, new GodsEyeGateToTheReikai());

        setupCombatWhereHerderDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent tendo = findPermanent(player2, "Tendo Ice Bridge");
        harness.handleMultiplePermanentsChosen(player2, List.of(tendo.getId()));

        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player2, "Gods' Eye, Gate to the Reikai");
    }

    @Test
    @DisplayName("Each player chooses one land when both have multiple lands")
    void eachPlayerChoosesOneLandWhenBothHaveMultiple() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player1, new GodsEyeGateToTheReikai());
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.addToBattlefield(player2, new GodsEyeGateToTheReikai());

        setupCombatWhereHerderDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        Permanent player1Tendo = findPermanent(player1, "Tendo Ice Bridge");
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Tendo.getId()));

        choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        Permanent player2Tendo = findPermanent(player2, "Tendo Ice Bridge");
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Tendo.getId()));

        harness.assertNotOnBattlefield(player1, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player1, "Gods' Eye, Gate to the Reikai");
        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player2, "Gods' Eye, Gate to the Reikai");
    }

    @Test
    @DisplayName("Non-land permanents are never sacrificed")
    void nonLandPermanentsAreNotSacrificed() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new GnarledMass());

        setupCombatWhereHerderDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Trigger only fires on death, not while it is on the battlefield")
    void noSacrificeWhileAlive() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player2, "Tendo Ice Bridge");
    }
}
