package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VhalEagerScholar.class, GrizzlyBears.class, Island.class})
class VhalEagerScholarTest extends BaseCardTest {

    @Test
    void lootingPutsStudyCounterOnVhal() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(vhal.getCounterCount(CounterType.STUDY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void blueSpecializationRemovesStudyCountersAndLooksAtThatManyCards() {
        Permanent vhal = addCreatureReady(player1, new VhalEagerScholar());
        vhal.setCounterCount(CounterType.STUDY, 2);
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vhal.getCard().getName()).isEqualTo("Vhal, Scholar of Prophecy");
        assertThat(vhal.getCounterCount(CounterType.STUDY)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(com.github.laxika.magicalvibes.model.PendingInteraction.LibraryRevealChoice.class);
    }
}
