package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeiferBalambRival.class, GrizzlyBears.class})
class SeiferBalambRivalTest extends BaseCardTest {

    @Test
    void attackTriggerGoadsTargetCreatureDefendingPlayerControls() {
        addCreatureReady(player1, new SeiferBalambRival());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    void attackerGainsDeathtouchWhenBlockedByTwoCreatures() {
        Permanent seifer = addCreatureReady(player1, new SeiferBalambRival());
        Permanent blocker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker2 = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, seifer, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, blocker1, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, blocker2, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void attackerDoesNotGainDeathtouchWhenBlockedByOneCreature() {
        Permanent seifer = addCreatureReady(player1, new SeiferBalambRival());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, seifer, Keyword.DEATHTOUCH)).isFalse();
    }
}
