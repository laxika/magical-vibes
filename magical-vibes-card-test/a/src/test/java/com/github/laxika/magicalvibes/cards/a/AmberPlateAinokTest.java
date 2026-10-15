package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AmberPlateAinok.class)
class AmberPlateAinokTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Amber-Plate Ainok endures with a +1/+1 counter in the second main phase")
    void tappedAinokEnduresWithCounter() {
        Permanent ainok = addAinokReady(player1);
        ainok.tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 1 +1/+1 counter on this permanent");

        assertThat(ainok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Amber-Plate Ainok can endure by creating a Spirit")
    void tappedAinokEnduresWithSpirit() {
        addAinokReady(player1).tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 1/1 white Spirit creature token");

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("An untapped Amber-Plate Ainok does not endure")
    void untappedAinokDoesNotEndure() {
        addAinokReady(player1);

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Double team conjures a copy without double team")
    void doubleTeamConjuresCopy() {
        Permanent ainok = addAinokReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ainok, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Amber-Plate Ainok"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }

    private Permanent addAinokReady(Player player) {
        return addCreatureReady(player, new AmberPlateAinok());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
