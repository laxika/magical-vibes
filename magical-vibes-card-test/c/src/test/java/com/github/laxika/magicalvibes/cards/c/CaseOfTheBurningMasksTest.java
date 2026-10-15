package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseOfTheBurningMasks.class, GiantSpider.class, LightningStrike.class, Shock.class})
class CaseOfTheBurningMasksTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToOpponentsCreature() {
        addCaseAfterItsDamageResolves();

        assertThat(findPermanent(player2, "Giant Spider").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotTargetControllersCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new CaseOfTheBurningMasks()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotSolveWithOnlyTwoDamageSources() {
        Permanent casePermanent = addCaseAfterItsDamageResolves();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        castLightningStrike(target);
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void opponentsDamageSourcesDoNotSolveTheCase() {
        Permanent casePermanent = addCaseAfterItsDamageResolves();
        harness.addMana(player2, ManaColor.RED, 2);
        for (int i = 0; i < 2; i++) {
            harness.setHand(player2, List.of(new Shock()));
            harness.castInstant(player2, 0, player1.getId());
            harness.passBothPriorities();
        }
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
        harness.assertLife(player1, 16);
    }

    @Test
    void cannotActivateWhileUnsolved() {
        addCaseAfterItsDamageResolves();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Case of the Burning Masks");
    }

    @Test
    void chosenCardCanBeCastButOtherExiledCardsCannot() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheBurningMasks());
        casePermanent.setSolved(true);
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, first.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(second.getId());
        harness.assertInGraveyard(player1, "Case of the Burning Masks");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void chosenCardCannotBeCastAfterTheTurnEnds() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheBurningMasks());
        casePermanent.setSolved(true);
        Card chosen = new Shock();
        harness.setLibrary(player1, List.of(chosen));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(chosen.getId());
    }

    @Test
    void emptyLibraryStillSacrificesCaseWithoutOfferingAChoice() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheBurningMasks());
        casePermanent.setSolved(true);
        harness.setLibrary(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Case of the Burning Masks");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Solves after three different sources deal damage this turn")
    void solvesAfterThreeDifferentSourcesDealDamage() {
        Permanent casePermanent = addCaseAfterItsDamageResolves();
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent thirdTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castLightningStrike(secondTarget);
        castLightningStrike(thirdTarget);
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("The solved ability sacrifices the Case and offers one of the top three cards to play")
    void solvedAbilityExilesThreeAndOffersOneCard() {
        addCaseAfterItsDamageResolves();
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent thirdTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        castLightningStrike(secondTarget);
        castLightningStrike(thirdTarget);
        resolveEndStepTriggers();

        Card first = new Shock();
        Card second = new Shock();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Case of the Burning Masks")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
    }

    private Permanent addCaseAfterItsDamageResolves() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CaseOfTheBurningMasks()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Case of the Burning Masks");
    }

    private void castLightningStrike(Permanent target) {
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
