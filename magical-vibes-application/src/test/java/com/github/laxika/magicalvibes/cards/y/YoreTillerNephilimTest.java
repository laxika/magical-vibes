package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoreTillerNephilim.class, GruulNodorog.class, GruulTurf.class})
class YoreTillerNephilimTest extends BaseCardTest {

    @Test
    void onlyCreatureCardsCanBeTargeted() {
        Card creature = new GruulNodorog();
        Card land = new GruulTurf();
        harness.setGraveyard(player1, List.of(creature, land));
        addCreatureReady(player1, new YoreTillerNephilim());

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void returnsChosenCreatureTappedAndAttacking() {
        Card creature = new GruulNodorog();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new YoreTillerNephilim());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            resolveAllTriggers();
        });

        Permanent returned = findPermanent(player1, "Gruul Nodorog");
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isFalse();
        assertThat(returned.getAttacksThisTurn()).isZero();
        assertThat(returned.getAttacksThisGame()).isZero();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player1, "Gruul Nodorog");
    }

    @Test
    void returnedCreatureDealsCombatDamageWithoutHaste() {
        Card creature = new GruulNodorog();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new YoreTillerNephilim());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            resolveAllTriggers();
        });
        resolveCombat();

        harness.assertLife(player2, 14);
        harness.assertOnBattlefield(player1, "Gruul Nodorog");
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Card creature = new GruulNodorog();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new YoreTillerNephilim());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            harness.setGraveyard(player1, List.of());
            resolveAllTriggers();
        });

        harness.assertNotOnBattlefield(player1, "Gruul Nodorog");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnedNephilimDoesNotTriggerItsAttackAbility() {
        Card returnedNephilim = new YoreTillerNephilim();
        Card otherCreature = new GruulNodorog();
        harness.setGraveyard(player1, List.of(returnedNephilim, otherCreature));
        addCreatureReady(player1, new YoreTillerNephilim());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultipleCardsChosen(player1, List.of(returnedNephilim.getId()));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Yore-Tiller Nephilim")).hasSize(2);
        Permanent returned = findPermanents(player1, "Yore-Tiller Nephilim").stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedNephilim.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        harness.assertInGraveyard(player1, "Gruul Nodorog");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerWhenNoCreatureCardMatches() {
        harness.setGraveyard(player1, List.of(new GruulTurf()));
        addCreatureReady(player1, new YoreTillerNephilim());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Gruul Turf");
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GruulNodorog()));
        addCreatureReady(player1, new YoreTillerNephilim());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Gruul Nodorog");
    }

    @Test
    void doesNotTriggerWhenThisCreatureDoesNotAttack() {
        Card creature = new GruulNodorog();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new YoreTillerNephilim());

        declareAttackers(List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Gruul Nodorog");
    }
}
