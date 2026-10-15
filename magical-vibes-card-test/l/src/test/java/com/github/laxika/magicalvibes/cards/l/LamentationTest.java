package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EvolutionSage;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
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

@CardUsed({Lamentation.class, EvolutionSage.class, InfernalGrasp.class})
class LamentationTest extends BaseCardTest {

    @Test
    @DisplayName("Enters, destroys an opponent creature, and gains 3 life")
    void entersDestroysOpponentCreatureAndGainsLife() {
        Permanent target = addCreatureReady(player2, new EvolutionSage());
        harness.setHand(player1, List.of(new Lamentation()));
        addManaForCast();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Evolution Sage");
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its caster")
    void cannotTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new EvolutionSage());
        harness.setHand(player1, List.of(new Lamentation()));
        addManaForCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Encore creates an untapped hasty token and sacrifices it at the next end step")
    void encoreCreatesUntappedTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Lamentation()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Lamentation");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lamentation")).isEmpty();
    }

    @Test
    @DisplayName("Entering without an opposing creature does not gain life")
    void noLegalTargetDoesNotGainLife() {
        harness.setHand(player1, List.of(new Lamentation()));
        addManaForCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lamentation");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An illegal target prevents the entire enter trigger from resolving")
    void removedTargetPreventsLifeGain() {
        Permanent target = addCreatureReady(player2, new EvolutionSage());
        harness.setHand(player1, List.of(new Lamentation()));
        addManaForCast();
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Evolution Sage");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Encore exiles its source as a cost and its token triggers the enter ability")
    void encorePaysExileCostAndTokenDestroysCreature() {
        Permanent target = addCreatureReady(player2, new EvolutionSage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Lamentation()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        harness.assertNotInGraveyard(player1, "Lamentation");
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getName()).containsExactly("Lamentation");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Evolution Sage");
        harness.assertLife(player1, 23);
        assertThat(findPermanent(player1, "Lamentation").getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Encore tokens must attack if able")
    void encoreTokenCannotBeOmittedFromAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Lamentation()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore activated after combat does not create an attacking token")
    void encoreAfterCombatDoesNotEnterAttacking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Lamentation()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Lamentation");
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isTapped()).isFalse();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Lamentation");
    }

    @Test
    @DisplayName("Encore cannot be activated outside a main phase")
    void encoreRequiresSorceryTiming() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setGraveyard(player1, List.of(new Lamentation()));
        addManaForEncore();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Lamentation");
        assertThat(gd.exiledCards).isEmpty();
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
