package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StarCrownedStagTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps a chosen creature the defending player controls")
    void tapsDefendingCreature() {
        addCreatureReady(player1, new StarCrownedStag());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only creatures the defending player controls are legal targets")
    void ownCreaturesAreNotLegalTargets() {
        Permanent attacker = addCreatureReady(player1, new StarCrownedStag());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId())
                .doesNotContain(ownBears.getId(), attacker.getId());
    }

    @Test
    @DisplayName("No target selection when the defending player controls no creature")
    void noTargetWithoutDefendingCreature() {
        addCreatureReady(player1, new StarCrownedStag());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }
}
