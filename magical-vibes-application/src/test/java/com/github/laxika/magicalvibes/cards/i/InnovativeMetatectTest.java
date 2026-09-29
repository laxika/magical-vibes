package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnovativeMetatect.class, AlphaMyr.class, GrizzlyBears.class, Forest.class, HillGiant.class})
class InnovativeMetatectTest extends BaseCardTest {

    @Test
    void artifactCreatureCombatDamageSeeksOneEligibleCard() {
        Card eligible = new GrizzlyBears();
        Card land = new Forest();
        Card tooExpensive = new HillGiant();
        harness.setLibrary(player1, List.of(eligible, land, tooExpensive));
        addCreatureReady(player1, new InnovativeMetatect());
        addCreatureReady(player1, new AlphaMyr());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(eligible.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    void multipleArtifactCreaturesCauseOnlyOneSeek() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        addCreatureReady(player1, new InnovativeMetatect());
        addCreatureReady(player1, new AlphaMyr());
        addCreatureReady(player1, new AlphaMyr());

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void nonartifactCreatureCombatDamageDoesNotTrigger() {
        Card card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        addCreatureReady(player1, new InnovativeMetatect());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }
}
