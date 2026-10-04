package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GnollHuntingParty.class, GrizzlyBears.class})
class GnollHuntingPartyTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each creature you attacked with this turn")
    void costIsReducedByYourAttackingCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GnollHuntingParty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires its full cost without attacking")
    void fullCostIsRequiredWithoutAttacking() {
        harness.setHand(player1, List.of(new GnollHuntingParty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Has first strike during its controller's turn only")
    void hasFirstStrikeDuringControllerTurnOnly() {
        Permanent gnoll = addCreatureReady(player1, new GnollHuntingParty());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, gnoll, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, gnoll, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double team conjures a copy without double team")
    void doubleTeamConjuresCopy() {
        Permanent gnoll = addCreatureReady(player1, new GnollHuntingParty());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, gnoll, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Gnoll Hunting Party"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }

    @Test
    @DisplayName("A creature attacking twice in one turn reduces the cost only once")
    void repeatedAttackerCountsOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new GnollHuntingParty());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        attacker.setTapped(false);
        attacker.setAttacking(false);
        gd.declaredAttackerIdsThisCombat.clear();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GnollHuntingParty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reduction cannot remove the red mana requirement")
    void reductionPreservesColoredCost() {
        for (int i = 0; i < 6; i++) {
            addCreatureReady(player1, new GnollHuntingParty());
        }
        declareAttackers(List.of(0, 1, 2, 3, 4, 5));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GnollHuntingParty()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A token with double team does not conjure a duplicate")
    void tokenDoesNotConjureDuplicate() {
        GnollHuntingParty token = new GnollHuntingParty();
        token.setToken(true);
        Permanent attacker = addCreatureReady(player1, token);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_TEAM)).isTrue();
    }

    @Test
    @DisplayName("Double team does not conjure again on a later attack")
    void doubleTeamTriggersOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new GnollHuntingParty());
        harness.setHand(player1, List.of());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        attacker.setTapped(false);
        attacker.setAttacking(false);
        gd.declaredAttackerIdsThisCombat.clear();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
