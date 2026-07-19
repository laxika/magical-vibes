package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HellkiteHatchlingTest extends BaseCardTest {

    private void castHellkiteHatchling() {
        harness.setHand(player1, new ArrayList<>(List.of(new HellkiteHatchling())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> devour choice
    }

    private Permanent hellkite() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hellkite Hatchling"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Devouring a creature enters with a +1/+1 counter and gains flying and trample")
    void devourGrantsFlyingAndTrample() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHellkiteHatchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        Permanent hellkite = hellkite();
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters and has neither flying nor trample")
    void devourNoneNoKeywords() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHellkiteHatchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent hellkite = hellkite();
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.TRAMPLE)).isFalse();
    }
}
