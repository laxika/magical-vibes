package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.v.VesselOfNascency;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OngoingInvestigation.class, QuilledWolf.class, VesselOfNascency.class})
class OngoingInvestigationTest extends BaseCardTest {

    @Test
    void oneCreatureDealingCombatDamageCreatesAClue() {
        harness.addToBattlefield(player1, new OngoingInvestigation());
        addAttacker(new QuilledWolf());

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void multipleCreaturesDealingCombatDamageCreateOnlyOneClue() {
        harness.addToBattlefield(player1, new OngoingInvestigation());
        addAttacker(new QuilledWolf());
        addAttacker(new QuilledWolf());

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void activatedAbilityExilesCreatureCreatesClueAndGainsLife() {
        harness.addToBattlefield(player1, new OngoingInvestigation());
        harness.setGraveyard(player1, List.of(new QuilledWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Quilled Wolf"));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    private void addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }

    @Test
    void opposingCreatureCombatDamageDoesNotInvestigate() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new OngoingInvestigation());
        Permanent attacker = addCreatureReady(player2, new QuilledWolf());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotExileNoncreatureOrOpponentsCreatureToPayCost() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new OngoingInvestigation());
        harness.setGraveyard(player1, List.of(new VesselOfNascency()));
        harness.setGraveyard(player2, List.of(new QuilledWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void investigatedClueCanBeSacrificedForTwoManaToDraw() {
        harness.addToBattlefield(player1, new OngoingInvestigation());
        addAttacker(new QuilledWolf());
        resolveCombat();
        resolveAllTriggers();
        Card draw = new VesselOfNascency();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));

        harness.activateAbility(player1, clueIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void eachInvestigationTriggersSeparatelyForTheSameCombatDamage() {
        harness.addToBattlefield(player1, new OngoingInvestigation());
        harness.addToBattlefield(player1, new OngoingInvestigation());
        addAttacker(new QuilledWolf());
        addAttacker(new QuilledWolf());

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void creatureIsExiledAsCostBeforeInvestigationAndLifeGainResolve() {
        harness.addToBattlefield(player1, new OngoingInvestigation());
        Card creature = new QuilledWolf();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
