package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeavyweightDemolisherTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3} during upkeep keeps Heavyweight Demolisher untapped")
    void payingUpkeepCostKeepsItUntapped() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(demolisher.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep payment taps Heavyweight Demolisher")
    void decliningUpkeepCostTapsIt() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(demolisher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Being unable to pay the upkeep cost taps Heavyweight Demolisher")
    void insufficientManaTapsIt() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(demolisher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void noTriggerDuringOpponentUpkeep() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(demolisher.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unearth returns Heavyweight Demolisher with haste and exiles it at the next end step")
    void unearthReturnsAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new HeavyweightDemolisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent demolisher = findPermanent(player1, "Heavyweight Demolisher");
        assertThat(demolisher.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Heavyweight Demolisher");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Heavyweight Demolisher"));
    }
}
