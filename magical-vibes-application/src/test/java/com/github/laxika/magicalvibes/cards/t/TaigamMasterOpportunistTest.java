package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaigamMasterOpportunistTest extends BaseCardTest {

    @Test
    void copiesTheSecondSpellAndSuspendsTheOriginal() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        LightningBolt first = new LightningBolt();
        LightningBolt second = new LightningBolt();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.suspendedSpellExiles)
                .anySatisfy(pending -> {
                    assertThat(pending.cardId()).isEqualTo(second.getId());
                    assertThat(pending.counters()).isEqualTo(4);
                });
        assertThat(gd.stack).filteredOn(com.github.laxika.magicalvibes.model.StackEntry::isCopy).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void removesTimeCountersAndOffersTheSpellAfterTheLastOne() {
        harness.addToBattlefield(player1, new TaigamMasterOpportunist());
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        for (int expectedCounters = 3; expectedCounters > 0; expectedCounters--) {
            int counters = expectedCounters;
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            assertThat(gd.suspendedSpellExiles).anySatisfy(pending -> {
                assertThat(pending.cardId()).isEqualTo(second.getId());
                assertThat(pending.counters()).isEqualTo(counters);
            });
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(second.getId()));
    }
}
