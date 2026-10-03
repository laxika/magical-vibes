package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.SacrificePermanentAtControllerEndStepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshlingTheLimitless.class, AirElemental.class, Mulldrifter.class})
class AshlingTheLimitlessTest extends BaseCardTest {

    @Test
    void grantsEvokeAndCopiesTheSacrificedElementalWithHaste() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Air Elemental")).hasSize(1);
        Permanent token = findPermanents(player1, "Air Elemental").getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(SacrificePermanentAtControllerEndStepUnlessPays.class))
                .hasSize(1);
    }

    @Test
    void tokenIsSacrificedAtItsControllersNextEndStepUnlessPaidFor() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Air Elemental")).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    void payingAllFiveColorsKeepsTheTokenAndHasteExpiresAfterTheTurn() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Air Elemental");

        advanceToEndStep(player1);
        harness.passBothPriorities();
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Air Elemental").getId()).isEqualTo(token.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(token.hasKeyword(Keyword.HASTE)).isFalse();
        advanceToEndStep(player1);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Air Elemental").getId()).isEqualTo(token.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsEndStepDoesNotSacrificeTheToken() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        advanceToEndStep(player2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Air Elemental")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(findPermanents(player1, "Air Elemental")).isEmpty();
        assertThat(findPermanents(player1, "Ashling, the Limitless")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedEvokeStillSacrificesAfterAshlingLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithEvoke(player1, 0, null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Ashling, the Limitless"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Air Elemental")).isEmpty();
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    void payingTheNormalManaCostDoesNotSacrificeTheElemental() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Air Elemental")).hasSize(1);
        assertThat(findPermanent(player1, "Air Elemental").getCard().isToken()).isFalse();
        advanceToEndStep(player1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void anElementalGoingToTheGraveyardWithoutBeingSacrificedDoesNotCreateACopy() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.addToBattlefield(player1, new AirElemental());

        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Air Elemental"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Air Elemental")).isEmpty();
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    void sacrificingAshlingCopiesItselfButSacrificingItsTokenDoesNotCopyAgain() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        Permanent ashling = findPermanent(player1, "Ashling, the Limitless");
        assertThat(harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, ashling)).isTrue();
        harness.getTriggerCollectionService().checkAllyPermanentSacrificedTriggers(
                gd, player1.getId(), ashling.getCard());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Ashling, the Limitless");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Ashling, the Limitless")).isEmpty();
    }

    @Test
    @CardUsed({AshlingTheLimitless.class, Mulldrifter.class})
    void canChooseGrantedColorlessEvokeEvenWhenTheCardHasNativeEvoke() {
        harness.addToBattlefield(player1, new AshlingTheLimitless());
        harness.setHand(player1, List.of(new Mulldrifter()));
        harness.setLibrary(player1, List.of(new AshlingTheLimitless(), new AshlingTheLimitless(),
                new AshlingTheLimitless(), new AshlingTheLimitless()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mulldrifter")).hasSize(1);
        assertThat(findPermanent(player1, "Mulldrifter").getCard().isToken()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Mulldrifter");
    }
}
