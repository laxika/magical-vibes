package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiperWrightPublickReporter.class, GrizzlyBears.class, Clue.class})
class PiperWrightPublickReporterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage investigates that many times")
    void combatDamageInvestigatesThatManyTimes() {
        Permanent piper = addCreatureReady(player1, new PiperWrightPublickReporter());
        piper.setPowerModifier(1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing a Clue puts a counter on a target creature you control")
    void clueSacrificePutsCounterOnTargetCreatureYouControl() {
        Permanent piper = addCreatureReady(player1, new PiperWrightPublickReporter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent cluePermanent = harness.addToBattlefieldAndReturn(player1, new Clue());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(piper.getId(), bears.getId())
                .doesNotContain(cluePermanent.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
