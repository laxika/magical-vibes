package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.Repel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShiftyDoppelganger.class, DuskImp.class, Island.class, Repel.class})
class ShiftyDoppelgangerTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen creature enters with haste and returns Shifty Doppelganger after being sacrificed")
    void chosenCreatureIsSacrificedAndSourceReturns() {
        harness.addToBattlefield(player1, new ShiftyDoppelganger());
        harness.setHand(player1, List.of(new DuskImp()));
        addActivationMana();

        activateAndChooseCreature();

        Permanent duskImp = findPermanent(player1, "Dusk Imp");
        assertThat(duskImp.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Dusk Imp");
        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertOnBattlefield(player1, "Shifty Doppelganger");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));
    }

    @Test
    @DisplayName("The hand choice offers creature cards but not noncreature cards")
    void onlyCreatureCardsCanBeChosen() {
        harness.addToBattlefield(player1, new ShiftyDoppelganger());
        harness.setHand(player1, List.of(new Island(), new DuskImp()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Accepting with no creature card in hand leaves Shifty Doppelganger exiled")
    void acceptingWithNoCreatureCardLeavesSourceExiled() {
        harness.addToBattlefield(player1, new ShiftyDoppelganger());
        harness.setHand(player1, List.of(new Island()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Shifty Doppelganger");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("If the chosen creature leaves before the end step, Shifty Doppelganger stays exiled")
    void sourceDoesNotReturnWhenChosenCreatureLeavesBeforeEndStep() {
        harness.addToBattlefield(player1, new ShiftyDoppelganger());
        harness.setHand(player1, List.of(new DuskImp()));
        addActivationMana();

        activateAndChooseCreature();

        Permanent duskImp = findPermanent(player1, "Dusk Imp");
        harness.setHand(player1, List.of(new Repel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, duskImp.getId());
        harness.passBothPriorities();

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Dusk Imp");
        harness.assertNotOnBattlefield(player1, "Shifty Doppelganger");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));
    }

    @Test
    @DisplayName("Declining the creature choice leaves Shifty Doppelganger exiled")
    void decliningLeavesSourceExiled() {
        harness.addToBattlefield(player1, new ShiftyDoppelganger());
        harness.setHand(player1, List.of(new DuskImp()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Shifty Doppelganger");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));
        harness.assertInHand(player1, "Dusk Imp");
    }

    private void activateAndChooseCreature() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
