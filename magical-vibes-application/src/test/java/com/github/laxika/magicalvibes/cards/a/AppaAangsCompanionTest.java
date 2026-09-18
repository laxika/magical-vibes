package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AppaAangsCompanion.class, AirElemental.class, GrizzlyBears.class})
class AppaAangsCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another attacking creature without flying")
    void attackTriggerRestrictsTargets() {
        Permanent appa = addCreatureReady(player1, new AppaAangsCompanion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId())
                .doesNotContain(appa.getId());
    }

    @Test
    @DisplayName("Attack trigger grants flying until end of turn")
    void attackTriggerGrantsFlying() {
        addCreatureReady(player1, new AppaAangsCompanion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A creature with flying cannot be targeted")
    void cannotTargetCreatureWithFlying() {
        addCreatureReady(player1, new AppaAangsCompanion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent flyingCreature = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, flyingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }
}
