package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DennickPiousApprentice.class, DennickPiousApparition.class, Disentomb.class,
        GrizzlyBears.class, Shock.class, Cancel.class, Consider.class, ReturnToNature.class})
class DennickPiousApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts Dennick from the graveyard transformed")
    void disturbEntersTransformed() {
        Permanent dennick = castWithDisturb();

        assertThat(dennick.isTransformed()).isTrue();
        assertThat(dennick.getCard().getName()).isEqualTo("Dennick, Pious Apparition");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dennick's back face is exiled instead of going to the graveyard")
    void backFaceIsExiledInsteadOfGraveyard() {
        Permanent dennick = castWithDisturb();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dennick));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(dennick.getOriginalCard().getId());
    }

    @Test
    @DisplayName("Dennick's front face prevents graveyard targets")
    void frontFacePreventsGraveyardTargets() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new DennickPiousApprentice());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Back face investigates for an opponent creature card once each turn")
    void investigatesForOpponentCreatureCardOnceEachTurn() {
        Permanent dennick = castWithDisturb();
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, firstBear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castAndResolveInstant(player1, 0, secondBear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(dennick.isTransformed()).isTrue();
    }

    @Test
    void frontFaceProtectsOpponentGraveyardFromExileTargets() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new DennickPiousApprentice());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void backFaceAllowsGraveyardTargets() {
        castWithDisturb();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void investigateLimitIsSharedAcrossBothPlayersGraveyards() {
        castWithDisturb();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, ownBear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castAndResolveInstant(player1, 0, opposingBear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesAgainOnOpponentsTurn() {
        castWithDisturb();
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, firstBear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.setLibrary(player2, List.of(new Shock()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, secondBear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void investigatesForCreatureCardSurveilledFromLibrary() {
        castWithDisturb();
        Card creature = new GrizzlyBears();
        Card nextCard = new Shock();
        harness.setLibrary(player1, List.of(creature, nextCard));
        harness.setHand(player1, List.of(new Consider()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void noncreatureGraveyardEntryDoesNotConsumeInvestigateTrigger() {
        castWithDisturb();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.castAndResolveInstant(player1, 0, bear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void frontFaceDoesNotPreventNontargetedSurveil() {
        harness.addToBattlefield(player1, new DennickPiousApprentice());
        Card creature = new GrizzlyBears();
        Card nextCard = new Shock();
        harness.setLibrary(player1, List.of(creature, nextCard));
        harness.setHand(player1, List.of(new Consider()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void counteredDisturbSpellIsExiled() {
        Card dennick = new DennickPiousApprentice();
        harness.setGraveyard(player1, List.of(dennick));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, dennick.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(dennick.getId());
    }

    @Test
    void frontFaceDiesNormally() {
        Card dennick = new DennickPiousApprentice();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, dennick);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(dennick);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void graveyardProtectionDoesNotPreventDisturbCasting() {
        harness.addToBattlefield(player2, new DennickPiousApprentice());

        Permanent disturbed = castWithDisturb();

        assertThat(disturbed.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void frontFaceGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DennickPiousApprentice());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void investigatedClueCanBeSacrificedToDraw() {
        castWithDisturb();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        resolveAllTriggers();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private Permanent castWithDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DennickPiousApprentice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
