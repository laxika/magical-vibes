package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EscapedExperimentTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, an opponent creature gets -1/-0 for each artifact you control")
    void attackShrinksTargetForControlledArtifacts() {
        addReadyExperiment(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger counts artifacts controlled by Escaped Experiment's controller")
    void attackTriggerCountsOnlyOwnArtifacts() {
        addReadyExperiment(player1);
        harness.addToBattlefield(player2, new Spellbook());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger only allows creatures controlled by an opponent")
    void attackTriggerRestrictsTargets() {
        addReadyExperiment(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).doesNotContain(ownCreature.getId());
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyExperiment(com.github.laxika.magicalvibes.model.Player player) {
        Permanent experiment = new Permanent(new EscapedExperiment());
        experiment.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(experiment);
        return experiment;
    }
}
