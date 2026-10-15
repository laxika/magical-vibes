package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Jubilation.class, GrizzlyBears.class})
class JubilationTest extends BaseCardTest {

    @Test
    @DisplayName("Entering boosts your creatures and grants them trample until end of turn")
    void entersBoostsOwnCreaturesUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Jubilation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent jubilation = findPermanents(player1, "Jubilation").get(0);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, jubilation)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, jubilation)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, jubilation, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Encore creates a hasty untapped token and sacrifices it at the next end step")
    void encoreCreatesTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Jubilation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Jubilation").get(0);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Jubilation")).isEmpty();
    }

    @Test
    @DisplayName("Encore exiles its source as a cost and the token triggers the enter ability")
    void encorePaysExileCostAndTriggersEnterAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Jubilation source = new Jubilation();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Jubilation");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(findPermanents(player1, "Jubilation")).isEmpty();

        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Jubilation");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Encore tokens must be declared as attackers if able")
    void encoreTokenCannotBeOmittedFromAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Jubilation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore cannot be activated outside a main phase")
    void encoreCannotBeActivatedDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setGraveyard(player1, List.of(new Jubilation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Jubilation");
    }

    @Test
    @DisplayName("Creatures entering after the enter trigger resolves do not receive its effects")
    void enterAbilityDoesNotAffectLaterCreatures() {
        harness.setHand(player1, List.of(new Jubilation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new Jubilation());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Encore activated after combat still sacrifices its token at the next end step")
    void postcombatEncoreTokenIsSacrificed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Jubilation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Jubilation")).hasSize(1);

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Jubilation")).isEmpty();
    }
}
