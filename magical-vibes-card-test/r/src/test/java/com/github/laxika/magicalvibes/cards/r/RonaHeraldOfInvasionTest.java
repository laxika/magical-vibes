package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        RonaHeraldOfInvasion.class,
        RonaTolarianObliterator.class,
        Forest.class,
        GrizzlyBears.class,
        Shock.class
})
class RonaHeraldOfInvasionTest extends BaseCardTest {

    @Test
    void tapAbilityLoots() {
        Permanent rona = addReadyRona();
        Shock drawn = new Shock();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(rona.isTapped()).isTrue();
    }

    @Test
    void legendarySpellUntapsRona() {
        Permanent rona = addReadyRona();
        rona.tap();

        prepareMainPhase(player1);
        harness.castFromHand(player1, new RonaHeraldOfInvasion(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(rona.isTapped()).isFalse();
    }

    @Test
    void transformsWithPhyrexianManaAtSorcerySpeed() {
        Permanent rona = addReadyRona();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(rona.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedRonaMayPutExiledLandOntoBattlefieldUnderItsController() {
        Permanent rona = transformRona();
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.setHand(player2, List.of(shock, forest));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castAndResolveInstant(player2, 0, rona.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void transformedRonaMayCastExiledNonlandWithoutPayingManaCost() {
        Permanent rona = transformRona();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(shock, bears));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castAndResolveInstant(player2, 0, rona.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard() == bears);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void nonlegendarySpellDoesNotUntapRona() {
        Permanent rona = addReadyRona();
        rona.tap();
        prepareMainPhase(player1);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(rona.isTapped()).isTrue();
    }

    @Test
    void opponentsLegendarySpellDoesNotUntapRona() {
        Permanent rona = addReadyRona();
        rona.tap();
        prepareMainPhase(player2);

        harness.castFromHand(player2, new RonaHeraldOfInvasion(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(rona.isTapped()).isTrue();
    }

    @Test
    void newlyEnteredRonaCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new RonaHeraldOfInvasion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lootingWithEmptyHandDiscardsTheDrawnCard() {
        addReadyRona();
        Shock drawn = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void transformsUsingBlackManaWithoutPayingLifeOrUntapping() {
        Permanent rona = addReadyRona();
        rona.tap();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(rona.isTransformed()).isTrue();
        assertThat(rona.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTransformDuringOpponentsMainPhase() {
        Permanent rona = addReadyRona();
        prepareMainPhase(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rona.isTransformed()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTransformOutsideMainPhase() {
        addReadyRona();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTransformWhileAnotherAbilityIsOnStack() {
        addReadyRona();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void frontFaceDoesNotExileCardsWhenDamaged() {
        Permanent rona = addReadyRona();
        Forest forest = new Forest();
        harness.setHand(player2, List.of(new Shock(), forest));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castAndResolveInstant(player2, 0, rona.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forest);
        assertThat(gd.findExiledCard(forest.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningLandLeavesItExiled() {
        Permanent rona = transformRona();
        Forest forest = new Forest();
        harness.setHand(player2, List.of(new Shock(), forest));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castAndResolveInstant(player2, 0, rona.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
    }

    @Test
    void decliningNonlandLeavesItExiled() {
        Permanent rona = transformRona();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new Shock(), bears));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castAndResolveInstant(player2, 0, rona.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == bears);
    }

    @Test
    void damageFromOwnSpellExilesFromOwnHand() {
        Permanent rona = transformRona();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new Shock(), forest));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);

        harness.castAndResolveInstant(player1, 0, rona.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
    }

    @Test
    void emptyDamageSourceControllersHandProducesNoOffer() {
        Permanent rona = transformRona();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castAndResolveInstant(player2, 0, rona.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exiledInstantCanChooseANewTargetAndGoesToItsOwnersGraveyard() {
        Permanent rona = transformRona();
        Shock exiledShock = new Shock();
        harness.setHand(player2, List.of(new Shock(), exiledShock));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castAndResolveInstant(player2, 0, rona.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.findExiledCard(exiledShock.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiledShock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(exiledShock);
    }

    @Test
    void combatDamageExilesFromSourceControllerEvenWhenSourceDiesInCombat() {
        transformRona();
        GrizzlyBears attacker = new GrizzlyBears();
        addCreatureReady(player2, attacker);
        Forest forest = new Forest();
        harness.setHand(player2, List.of(forest));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
    }

    private Permanent addReadyRona() {
        return addCreatureReady(player1, new RonaHeraldOfInvasion());
    }

    private Permanent transformRona() {
        Permanent rona = addReadyRona();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(rona.isTransformed()).isTrue();
        return rona;
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
