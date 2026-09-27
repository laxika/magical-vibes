package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritOfResilience.class, Regrowth.class, HillGiant.class, MindStone.class})
class SpiritOfResilienceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts on a counter, then may become a copy of a creature card that left the graveyard")
    void putsCounterAndCopiesCreatureCard() {
        Permanent spirit = addSpirit();
        HillGiant giant = new HillGiant();
        returnCardFromGraveyard(giant);

        resolveTrigger();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spirit.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the copy still keeps the counter")
    void decliningCopyKeepsCounter() {
        Permanent spirit = addSpirit();
        returnCardFromGraveyard(new HillGiant());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(spirit.getCard().getName()).isEqualTo("Spirit of Resilience");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can become a copy of an artifact card that left the graveyard")
    void copiesArtifactCard() {
        Permanent spirit = addSpirit();
        returnCardFromGraveyard(new MindStone());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spirit.getCard().getName()).isEqualTo("Mind Stone");
    }

    @Test
    @DisplayName("The copy wears off at end of turn")
    void copyWearsOffAtEndOfTurn() {
        Permanent spirit = addSpirit();
        returnCardFromGraveyard(new HillGiant());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spirit.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spirit.getCard().getName()).isEqualTo("Spirit of Resilience");
    }

    private Permanent addSpirit() {
        return harness.addToBattlefieldAndReturn(player1, new SpiritOfResilience());
    }

    private void returnCardFromGraveyard(com.github.laxika.magicalvibes.model.Card card) {
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new Regrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, card.getId());
    }

    private void resolveTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
