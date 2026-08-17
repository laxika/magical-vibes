package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HellkiteWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to a creature controlled by the defending player")
    void attackTriggerDealsDamage() {
        addCreatureReady(player1, new HellkiteWhelp());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only creatures controlled by the defending player are legal targets")
    void targetsOnlyDefendingCreatures() {
        addCreatureReady(player1, new HellkiteWhelp());
        Permanent ownCreature = addCreatureReady(player1, new LlanowarElves());
        Permanent defendingCreature = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("No target selection occurs when the defending player controls no creatures")
    void noLegalTargetSkipsTrigger() {
        addCreatureReady(player1, new HellkiteWhelp());

        declareAttackers(List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }
}
