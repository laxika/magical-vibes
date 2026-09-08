package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClammyProwler.class, GrizzlyBears.class})
class ClammyProwlerTest extends BaseCardTest {

    @Test
    void attackTriggerTargetsAnotherAttackingCreature() {
        Permanent prowler = addReadyCreature(player1, new ClammyProwler());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent nonattacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId())
                .doesNotContain(prowler.getId(), nonattacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(prowler.isCantBeBlocked()).isFalse();
    }

    @Test
    void doesNotTriggerWhenThereIsNoOtherAttackingCreature() {
        Permanent prowler = addReadyCreature(player1, new ClammyProwler());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(prowler.isCantBeBlocked()).isFalse();
    }

    @Test
    void unblockableWearsOffAtEndOfTurn() {
        addReadyCreature(player1, new ClammyProwler());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
