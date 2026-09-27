package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarrowingSwarm.class, Forest.class, GrizzlyBears.class})
class HarrowingSwarmTest extends BaseCardTest {

    @Test
    void manifestsAndReducesTurnFaceUpCostThenPutsCounterOnCreature() {
        Card manifestedCard = new GrizzlyBears();
        Card otherCard = new Forest();
        harness.setHand(player1, List.of(new HarrowingSwarm()));
        harness.setLibrary(player1, List.of(manifestedCard, otherCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
