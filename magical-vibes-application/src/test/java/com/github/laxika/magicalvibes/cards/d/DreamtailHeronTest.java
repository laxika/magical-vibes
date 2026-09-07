package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamtailHeron.class, GrizzlyBears.class})
class DreamtailHeronTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating draws a card")
    void mutatingDrawsACard() {
        Permanent heron = addCreatureReady(player1, new DreamtailHeron());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, heron, List.of(heron.getCard()), player1.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
    }
}
