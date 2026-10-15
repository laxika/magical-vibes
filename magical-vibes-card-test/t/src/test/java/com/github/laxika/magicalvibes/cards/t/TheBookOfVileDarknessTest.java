package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EyeOfVecna;
import com.github.laxika.magicalvibes.cards.h.HandOfVecna;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.j.JinnieFayJetmirsSecond;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBookOfVileDarkness.class, EyeOfVecna.class, HandOfVecna.class,
        HillGiantHerdgorger.class})
class TheBookOfVileDarknessTest extends BaseCardTest {

    @Test
    void createsZombieAtEndStepAfterControllerLosesTwoLife() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void doesNotCreateZombieWhenControllerLostOnlyOneLife() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void createsVecnaAndCopiesTriggeredAbilitiesFromCardsExiledAsCosts() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, artifactWithTriggeredDraw("Eye of Vecna"));
        harness.addToBattlefield(player1, artifact("Hand of Vecna"));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vecna")).hasSize(1);
        Permanent vecna = findPermanent(player1, "Vecna");
        assertThat(vecna.getCard().getPower()).isEqualTo(8);
        assertThat(vecna.getCard().getToughness()).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertNotOnBattlefield(player1, "Eye of Vecna");
        harness.assertNotOnBattlefield(player1, "Hand of Vecna");
    }

    @Test
    @CardUsed(JinnieFayJetmirsSecond.class)
    void retainsVecnasTriggeredAbilitiesWhenTokenReplacementIsDeclined() {
        activateBookWithTokenReplacement();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleListChoice(player1, "Original tokens");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vecna")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @CardUsed(JinnieFayJetmirsSecond.class)
    void replacementCatDoesNotGainVecnasTriggeredAbilities() {
        activateBookWithTokenReplacement();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleListChoice(player1, "Cat");

        assertThat(findPermanents(player1, "Cat")).hasSize(1);
        assertThat(findPermanents(player1, "Vecna")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void realEyeEntryAbilityDrawsAndLosesLifeAndBookAbilityCreatesZombie() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        createVecnaWithRealArtifacts();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertNotOnBattlefield(player1, "Eye of Vecna");
        harness.assertNotOnBattlefield(player1, "Hand of Vecna");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    void vecnaInheritedHandAbilityBoostsItByHandSizeAtCombat() {
        createVecnaWithRealArtifacts();
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));
        Permanent vecna = findPermanent(player1, "Vecna");

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(10);
    }

    @Test
    void vecnaInheritedEyeUpkeepAbilityCanBePaid() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));
        createVecnaWithRealArtifacts();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertLife(player1, 16);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void vecnaInheritedEyeUpkeepAbilityCanBeDeclined() {
        harness.setLife(player1, 20);
        createVecnaWithRealArtifacts();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void controllerLifeLossDoesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void losingLifeAfterEndStepBeginsDoesNotRetroactivelyTrigger() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }


    @Test
    void cannotActivateWithoutHandOfVecna() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertOnBattlefield(player1, "Eye of Vecna");
        assertThat(findPermanents(player1, "Vecna")).isEmpty();
    }

    @Test
    void cannotExileOpponentsEyeToPayActivationCost() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, new HandOfVecna());
        harness.addToBattlefield(player2, new EyeOfVecna());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertOnBattlefield(player1, "Hand of Vecna");
        harness.assertOnBattlefield(player2, "Eye of Vecna");
        assertThat(findPermanents(player1, "Vecna")).isEmpty();
    }

    @Test
    void exilesAllThreeArtifactsBeforeCreatingIndestructibleVecna() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.addToBattlefield(player1, new HandOfVecna());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertNotOnBattlefield(player1, "Eye of Vecna");
        harness.assertNotOnBattlefield(player1, "Hand of Vecna");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("The Book of Vile Darkness", "Eye of Vecna", "Hand of Vecna");
        assertThat(findPermanents(player1, "Vecna")).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vecna = findPermanent(player1, "Vecna");
        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(8);
        assertThat(vecna.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(vecna.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.GOD);
        assertThat(vecna.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.hasKeyword(gd, vecna, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void lifeGainDoesNotUndoLifeLostForInheritedBookTrigger() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        createVecnaWithRealArtifacts();
        harness.setHand(player1, List.of(new HillGiantHerdgorger()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 21);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    void cannotActivateWithoutEyeOfVecna() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, new HandOfVecna());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertOnBattlefield(player1, "Hand of Vecna");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateTappedBook() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.addToBattlefield(player1, new HandOfVecna());
        findPermanent(player1, "The Book of Vile Darkness").tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertOnBattlefield(player1, "Eye of Vecna");
        harness.assertOnBattlefield(player1, "Hand of Vecna");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void createVecnaWithRealArtifacts() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.addToBattlefield(player1, new HandOfVecna());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void activateBookWithTokenReplacement() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, artifactWithTriggeredDraw("Eye of Vecna"));
        harness.addToBattlefield(player1, artifact("Hand of Vecna"));
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private Card artifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }

    private Card artifactWithTriggeredDraw(String name) {
        Card card = artifact(name);
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        return card;
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
