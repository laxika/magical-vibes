package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cloudpiercer.class, Forest.class, Mountain.class})
class CloudpiercerTest extends BaseCardTest {

    @Test
    void mutatingMayDiscardToDraw() {
        Mountain discarded = new Mountain();
        Forest drawn = new Forest();
        Permanent cloudpiercer = addCreatureReady(player1, new Cloudpiercer());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        triggerMutation(cloudpiercer);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void mutatingMayBeDeclined() {
        Mountain discarded = new Mountain();
        Forest drawn = new Forest();
        Permanent cloudpiercer = addCreatureReady(player1, new Cloudpiercer());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        triggerMutation(cloudpiercer);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discarded);
    }

    private void triggerMutation(Permanent cloudpiercer) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, cloudpiercer, List.of(cloudpiercer.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
