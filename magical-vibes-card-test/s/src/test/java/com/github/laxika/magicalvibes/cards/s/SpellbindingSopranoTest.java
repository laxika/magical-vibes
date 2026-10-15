package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({SpellbindingSoprano.class, AngelsMercy.class, Divination.class, RuneclawBear.class})
class SpellbindingSopranoTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking reduces instant and sorcery spells by {1} this turn")
    void attackReducesInstantAndSorcerySpells() {
        addCreatureReady(player1, new SpellbindingSoprano());
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AngelsMercy(), new Divination()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Attacking does not reduce creature spells")
    void attackDoesNotReduceCreatureSpells() {
        addCreatureReady(player1, new SpellbindingSoprano());
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack reduction expires at end of turn")
    void attackReductionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new SpellbindingSoprano());
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore creates an untapped hasty token that attacks normally and is sacrificed at the next end step")
    void encoreCreatesTokenThatAttacksAndIsSacrificed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SpellbindingSoprano()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.assertNotInGraveyard(player1, "Spellbinding Soprano");
        assertThat(gd.exiledCards).hasSize(1);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Spellbinding Soprano");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new SpellbindingSoprano(), new SpellbindingSoprano()));
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spellbinding Soprano")).isEmpty();
    }

    @Test
    @DisplayName("Encore tokens must attack if able")
    void encoreTokensCannotBeOmittedFromAttack() {
        harness.setGraveyard(player1, List.of(new SpellbindingSoprano()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore after combat creates a nonattacking token and still sacrifices it")
    void encoreAfterCombatDoesNotCreateAttackingCreature() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new SpellbindingSoprano()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Spellbinding Soprano");
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isTapped()).isFalse();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Spellbinding Soprano");
    }

    @Test
    @DisplayName("Multiple attack triggers stack their cost reductions")
    void multipleAttackTriggersStack() {
        addCreatureReady(player1, new SpellbindingSoprano());
        addCreatureReady(player1, new SpellbindingSoprano());
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The attack trigger does not reduce the opponent's spells")
    void attackDoesNotReduceOpponentsSpells() {
        addCreatureReady(player1, new SpellbindingSoprano());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AngelsMercy()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encore can only be activated at sorcery speed")
    void encoreRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new SpellbindingSoprano()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
