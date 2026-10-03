package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodsproutTalisman.class, GrizzlyBears.class, Forest.class, Unsummon.class, HillGiant.class})
class BloodsproutTalismanTest extends BaseCardTest {

    @Test
    void entersTapped() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());

        assertThat(talisman.isTapped()).isTrue();
    }

    @Test
    void choosesNonlandCardAndMakesItCostOneLessPerpetually() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        talisman.untap();
        GrizzlyBears chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new Forest(), chosenCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PerpetualCastCostHandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualCastCostHandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void paysLifeAndTapsBeforeChoosingACardOnResolution() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        talisman.untap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 19);
        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.PerpetualCastCostHandCardChoice.class)).isNull();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 19);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenHandContainsOnlyLands() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        talisman.untap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.PerpetualCastCostHandCardChoice.class)).isNull();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void choosesFromTheControllersHandAtResolution() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        talisman.untap();
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        PendingInteraction.PerpetualCastCostHandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualCastCostHandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
    }

    @Test
    void repeatedReductionsNeverRemoveColoredManaRequirements() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        for (int i = 0; i < 2; i++) {
            talisman.untap();
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void multipleActivationsStackTheirGenericCostReductions() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        harness.setHand(player1, List.of(new HillGiant()));

        for (int i = 0; i < 2; i++) {
            talisman.untap();
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void canChooseAnArtifactWithoutReducingItsColoredCost() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        talisman.untap();
        harness.setHand(player1, List.of(new BloodsproutTalisman()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bloodsprout Talisman")).isEqualTo(2);
        assertThat(findPermanents(player1, "Bloodsprout Talisman")).allMatch(Permanent::isTapped);
    }

    @Test
    void reductionFollowsTheChosenCardAfterReturningToHandButNotAnotherCopy() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new BloodsproutTalisman());
        talisman.untap();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
