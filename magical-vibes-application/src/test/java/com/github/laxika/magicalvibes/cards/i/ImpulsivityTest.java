package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Impulsivity.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class, CatharticReunion.class, RayOfCommand.class, Cancel.class})
class ImpulsivityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can target an instant or sorcery card in any graveyard")
    void etbTargetsAnyGraveyard() {
        Shock ownInstant = new Shock();
        CounselOfTheSoratami opponentSorcery = new CounselOfTheSoratami();
        GrizzlyBears invalidCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownInstant));
        harness.setGraveyard(player2, List.of(opponentSorcery, invalidCreature));
        harness.castFromHand(player1, new Impulsivity(), "{6}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                ownInstant.getId(), opponentSorcery.getId());
    }

    @Test
    @DisplayName("ETB casts the chosen spell for free and exiles it after resolution")
    void etbCastsForFreeAndExilesSpell() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Impulsivity(), "{6}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Encore creates an untapped hasty token and sacrifices it at the next end step")
    void encoreCreatesUntappedTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Impulsivity()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Impulsivity");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Impulsivity")).isEmpty();
    }

    @Test
    @DisplayName("ETB may decline casting the target")
    void etbMayDecline() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.castFromHand(player1, new Impulsivity(), "{6}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(shock);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB casts an opponent's sorcery for free and preserves ownership")
    void etbCastsSorcery() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Shock firstDraw = new Shock();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Shock()));
        harness.setGraveyard(player2, List.of(counsel));
        harness.castFromHand(player1, new Impulsivity(), "{6}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(counsel);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(counsel);
    }

    @Test
    @DisplayName("A target removed from the graveyard in response cannot be cast")
    void etbCannotCastRemovedTarget() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.castFromHand(player1, new Impulsivity(), "{6}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(shock));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shock);
    }

    @Test
    @DisplayName("Casting from the graveyard does not waive mandatory discard costs")
    void etbRequiresAdditionalDiscardCostBeforeCasting() {
        CatharticReunion reunion = new CatharticReunion();
        Shock discard1 = new Shock();
        GrizzlyBears discard2 = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(reunion));
        harness.setHand(player1, List.of(new Impulsivity(), discard1, discard2));
        addManaForCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(reunion.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        if (gd.stack.stream().anyMatch(entry -> entry.getCard().getId().equals(reunion.getId()))) {
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard1, discard2);
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discard1, discard2);
        } else {
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            assertThat(gd.playerHands.get(player1.getId())).contains(discard1, discard2);
        }
    }

    @Test
    @DisplayName("Encore exiles the source as a cost before creating tokens")
    void encoreExilesSourceAsCost() {
        Impulsivity source = new Impulsivity();
        harness.setGraveyard(player1, List.of(source));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        harness.assertNotOnBattlefield(player1, "Impulsivity");
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Impulsivity")).hasSize(1);
    }

    @Test
    @DisplayName("Encore cannot be activated outside a main phase")
    void encoreRequiresSorceryTiming() {
        harness.forceStep(TurnStep.UPKEEP);
        Impulsivity source = new Impulsivity();
        harness.setGraveyard(player1, List.of(source));
        addManaForEncore();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(source);
    }

    @Test
    @DisplayName("Encore cannot sacrifice a token an opponent controls at the next end step")
    void encoreCannotSacrificeStolenToken() {
        harness.setGraveyard(player1, List.of(new Impulsivity()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Impulsivity");
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    @Test
    @DisplayName("The spell cast by ETB is exiled even when countered")
    void etbExilesCounteredSpell() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.castFromHand(player1, new Impulsivity(), "{6}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, shock.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Encore tokens must be declared attacking their assigned opponent if able")
    void encoreCannotOmitAbleAttacker() {
        harness.setGraveyard(player1, List.of(new Impulsivity()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore token copies have the enter ability")
    void encoreTokenTriggersEtb() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(new Impulsivity()));
        harness.setGraveyard(player2, List.of(shock));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shock);
        assertThat(findPermanents(player1, "Impulsivity")).hasSize(1);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
    }
}
