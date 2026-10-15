package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DeepwoodDenizen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoathsomeCurator.class, GrizzlyBears.class, HillGiant.class, DeepwoodDenizen.class})
class LoathsomeCuratorTest extends BaseCardTest {

    @Test
    @DisplayName("Exploiting a creature destroys a target opposing creature with mana value 3 or less")
    void exploitDestroysMatchingCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castLoathsomeCurator();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Declining exploit does not create a destroy trigger")
    void decliningExploitDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLoathsomeCurator();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Exploit trigger skips an opposing creature with mana value greater than 3")
    void exploitTriggerSkipsHighManaValueCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castLoathsomeCurator();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Exploit trigger cannot target a creature controlled by its controller")
    void exploitTriggerRejectsControllerCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLoathsomeCurator();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exploiting itself still destroys an opposing creature")
    void exploitingItselfDestroysCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeepwoodDenizen());

        castLoathsomeCurator();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Loathsome Curator"));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Loathsome Curator");
        harness.assertNotOnBattlefield(player1, "Loathsome Curator");
        harness.assertInGraveyard(player2, "Deepwood Denizen");
    }

    @Test
    @DisplayName("Exploit can destroy a creature with mana value exactly three")
    void exploitDestroysCreatureAtManaValueBoundary() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DeepwoodDenizen());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeepwoodDenizen());

        castLoathsomeCurator();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deepwood Denizen");
        harness.assertInGraveyard(player2, "Deepwood Denizen");
        harness.assertOnBattlefield(player1, "Loathsome Curator");
    }

    private void castLoathsomeCurator() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new LoathsomeCurator(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
