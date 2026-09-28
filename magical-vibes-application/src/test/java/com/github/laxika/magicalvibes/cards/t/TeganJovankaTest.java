package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeganJovanka.class, Ornithopter.class, GrizzlyBears.class})
class TeganJovankaTest extends BaseCardTest {

    @Test
    @DisplayName("Tegan targets an attacking historic creature and grants both benefits")
    void targetsAttackingHistoricCreature() {
        addCreatureReady(player1, new TeganJovanka());
        Permanent historicAttacker = addCreatureReady(player1, new Ornithopter());
        Permanent nonHistoricAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactly(historicAttacker.getId())
                .doesNotContain(nonHistoricAttacker.getId());

        harness.handlePermanentChosen(player1, historicAttacker.getId());
        harness.passBothPriorities();

        assertThat(historicAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(historicAttacker.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, historicAttacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(nonHistoricAttacker.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nonHistoricAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Tegan's benefits wear off at end of turn")
    void benefitsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new TeganJovanka());
        Permanent historicAttacker = addCreatureReady(player1, new Ornithopter());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, historicAttacker.getId());
        harness.passBothPriorities();

        assertThat(historicAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, historicAttacker, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(historicAttacker.getPowerModifier()).isZero();
        assertThat(historicAttacker.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, historicAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
