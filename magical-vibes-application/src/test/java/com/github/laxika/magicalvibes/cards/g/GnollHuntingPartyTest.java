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
}
