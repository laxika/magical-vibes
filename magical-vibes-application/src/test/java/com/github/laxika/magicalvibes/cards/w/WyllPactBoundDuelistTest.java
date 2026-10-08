package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GraySlaad;
import com.github.laxika.magicalvibes.cards.i.ImprovisedWeaponry;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.y.YoungBlueDragon;
import com.github.laxika.magicalvibes.cards.y.YoungRedDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WyllPactBoundDuelist.class, Forest.class, GraySlaad.class, ImprovisedWeaponry.class, Island.class,
        LightningBolt.class, LiquimetalCoating.class, Mountain.class, Plains.class, PropheticPrism.class,
        Swamp.class, YoungBlueDragon.class, YoungRedDragon.class})
class WyllPactBoundDuelistTest extends BaseCardTest {

    @Test
    void specializationDiscardsTheChosenCard() {
        Forest discard = new Forest();
        specialize(4, discard);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    void specializesIntoCelestialPact() {
        assertThat(specialize(0, new Plains()).getCard().getName())
                .isEqualTo("Wyll of the Celestial Pact");
    }

    @Test
    void specializesIntoElderPact() {
        assertThat(specialize(1, new Island()).getCard().getName())
                .isEqualTo("Wyll of the Elder Pact");
    }

    @Test
    void specializesIntoFiendPact() {
        assertThat(specialize(2, new Swamp()).getCard().getName())
                .isEqualTo("Wyll of the Fiend Pact");
    }

    @Test
    void specializesIntoBladePact() {
        assertThat(specialize(3, new Mountain()).getCard().getName())
                .isEqualTo("Wyll of the Blade Pact");
    }

    @Test
    void specializesIntoFeyPact() {
        assertThat(specialize(4, new Forest()).getCard().getName())
                .isEqualTo("Wyll of the Fey Pact");
    }

    @Test
    void enteringStealsAnOpposingCreatureWithManaValueFour() {
        Permanent dragon = addCreatureReady(player2, new YoungRedDragon());
        harness.setHand(player1, List.of(new WyllPactBoundDuelist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, dragon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dragon);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dragon);
    }

    @Test
    void enteringCannotTargetCreatureWithManaValueFive() {
        Permanent dragon = addCreatureReady(player2, new YoungBlueDragon());
        harness.setHand(player1, List.of(new WyllPactBoundDuelist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, dragon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringCannotTargetOwnCreature() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        harness.setHand(player1, List.of(new WyllPactBoundDuelist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, dragon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void celestialPactCanReturnTheCreatureJustSacrificed() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        specialize(0, new Plains());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getCard().getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Young Red Dragon");
        assertThat(returned.getId()).isNotEqualTo(dragon.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
    }

    @Test
    void celestialPactSacrificeUsesARespondableDelayedTrigger() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        specialize(0, new Plains());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getCard().getId()));
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Young Red Dragon");
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Young Red Dragon");
        harness.assertInGraveyard(player1, "Young Red Dragon");
    }

    @Test
    void celestialPactHastePersistsPastCleanupForLowManaValueCreature() {
        Permanent slaad = addCreatureReady(player1, new GraySlaad());
        specialize(0, new Plains());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, slaad.getId());
        harness.handleMultipleCardsChosen(player1, List.of(slaad.getCard().getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent returned = findPermanent(player1, "Gray Slaad");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
    }

    @Test
    void elderPactReturnsAnInstantToHand() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        specialize(1, new Island());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bolt);
        harness.assertInGraveyard(player1, "Young Red Dragon");
    }

    @Test
    void fiendPactDrawsForControllerWhenOpponentDeclinesLifePayment() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains()));
        specialize(2, new Swamp());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void fiendPactOpponentCanPayFiveLifeToPreventDraw() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains()));
        specialize(2, new Swamp());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 15);
    }

    @Test
    void bladePactUntapsDuringSameResolutionAsSacrifice() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        Permanent wyll = specialize(3, new Mountain());
        wyll.setTapped(true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());

        assertThat(wyll.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    void feyPactBoostsDuringSameResolutionAsSacrifice() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        Permanent wyll = specialize(4, new Forest());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dragon.getId());

        assertThat(gqs.getEffectivePower(gd, wyll)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, wyll)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, wyll, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningFeyPactSacrificeDoesNotBoost() {
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        Permanent wyll = specialize(4, new Forest());
        int powerBefore = gqs.getEffectivePower(gd, wyll);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, wyll)).isEqualTo(powerBefore);
        assertThat(gqs.hasKeyword(gd, wyll, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
    }

    @Test
    void enteringCanStealANoncreatureArtifact() {
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new WyllPactBoundDuelist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, prism.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(prism);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(prism);
    }

    @Test
    void specializationCanDiscardAColoredNonlandCard() {
        YoungRedDragon discard = new YoungRedDragon();
        specialize(3, discard);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        harness.assertOnBattlefield(player1, "Wyll of the Blade Pact");
    }

    @Test
    void specializationCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new WyllPactBoundDuelist());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 4, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void elderPactReturnsASorceryToHandAfterSacrificingAnArtifact() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        ImprovisedWeaponry weaponry = new ImprovisedWeaponry();
        harness.setGraveyard(player1, List.of(weaponry));
        specialize(1, new Island());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, prism.getId());
        harness.handleMultipleCardsChosen(player1, List.of(weaponry.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(weaponry);
        harness.assertInGraveyard(player1, "Prophetic Prism");
    }

    @Test
    void specializedWyllCanSacrificeItselfIfItIsAnArtifact() {
        Permanent wyll = addCreatureReady(player1, new WyllPactBoundDuelist());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, null, wyll.getId());
        harness.passBothPriorities();
        specialize(wyll, 4, new Forest());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, wyll.getId());

        harness.assertNotOnBattlefield(player1, "Wyll of the Fey Pact");
        harness.assertInGraveyard(player1, "Wyll of the Fey Pact");
    }

    private Permanent specialize(int abilityIndex, Card discard) {
        return specialize(addCreatureReady(player1, new WyllPactBoundDuelist()), abilityIndex, discard);
    }

    private Permanent specialize(Permanent wyll, int abilityIndex, Card discard) {
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int wyllIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wyll);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, wyllIndex, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return wyll;
    }
}
