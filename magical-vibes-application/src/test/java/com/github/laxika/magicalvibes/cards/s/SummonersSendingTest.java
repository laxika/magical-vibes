package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonersSending.class, AirElemental.class, GrizzlyBears.class, Shock.class})
class SummonersSendingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature with mana value 4 or greater creates a countered Spirit")
    void createsCounteredSpiritForHighManaValueCreature() {
        AirElemental target = new AirElemental();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());

        resolveEndStepAbility(target);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Exiling a lower-mana-value creature creates a Spirit without a counter")
    void createsUncounteredSpiritForLowManaValueCreature() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());

        resolveEndStepAbility(target);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the optional ability leaves the creature card in the graveyard")
    void decliningLeavesGraveyardUnchanged() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("The ability is not put on the stack without a creature card target")
    void requiresCreatureCardTarget() {
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.addToBattlefield(player1, new SummonersSending());

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void resolveEndStepAbility(Card target) {
        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
