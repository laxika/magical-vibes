package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoilElemental.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class RoilElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may gain control of a target creature")
    void landfallGainsControlOfTargetCreature() {
        addRoilElemental();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Declining landfall leaves the target creature under its original control")
    void decliningLandfallDoesNotGainControl() {
        addRoilElemental();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Control ends when Roil Elemental leaves the battlefield")
    void controlEndsWhenSourceLeaves() {
        Permanent roil = addRoilElemental();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, roil.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Landfall rejects noncreature targets")
    void landfallRejectsNoncreatureTarget() {
        addRoilElemental();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("An opponent's land does not trigger Roil Elemental")
    void opponentsLandDoesNotTrigger() {
        addRoilElemental();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Removing Roil Elemental before resolution prevents gaining control")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent roil = addRoilElemental();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, roil.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves before resolution cannot be stolen")
    void targetLeavesBeforeTriggerResolves() {
        addRoilElemental();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Losing control of Roil Elemental releases its stolen creature")
    void controlEndsWhenSourceChangesController() {
        Permanent roil = addRoilElemental();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.addToBattlefield(player2, new RoilElemental());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.handlePermanentChosen(player2, roil.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(roil, bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(roil, bears);
    }

    @Test
    @DisplayName("Roil Elemental can target itself with its landfall ability")
    void landfallCanTargetSource() {
        Permanent roil = addRoilElemental();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, roil.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(roil);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(roil);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addRoilElemental() {
        return harness.addToBattlefieldAndReturn(player1, new RoilElemental());
    }
}
