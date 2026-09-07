package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MajesticAuricorn.class)
class MajesticAuricornTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating gains 4 life")
    void mutatingGainsFourLife() {
        Permanent auricorn = addCreatureReady(player1, new MajesticAuricorn());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, auricorn, List.of(auricorn.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }
}
