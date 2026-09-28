package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SouvenirSnatcher.class, FountainOfYouth.class, PhyrexianHulk.class})
class SouvenirSnatcherTest extends BaseCardTest {

    @Test
    void mutatingGainsControlOfTargetNoncreatureArtifact() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        triggerMutation(snatcher);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(fountain.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(fountain.getId()));
    }

    @Test
    void mutatingCannotTargetArtifactCreature() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        harness.addToBattlefield(player2, new FountainOfYouth());

        triggerMutation(snatcher);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hulk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void triggerMutation(Permanent snatcher) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, snatcher, List.of(snatcher.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
