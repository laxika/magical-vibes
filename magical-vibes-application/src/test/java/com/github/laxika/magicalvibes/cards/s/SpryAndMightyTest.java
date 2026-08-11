package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpryAndMightyTest extends BaseCardTest {

    @Test
    void choosesTwoCreaturesDrawsByPowerDifferenceAndGrantsTrample() {
        Permanent smaller = addCreature(2);
        Permanent larger = addCreature(5);
        Permanent unchosen = addCreature(1);
        harness.setHand(player1, List.of(new SpryAndMighty()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(smaller.getId(), larger.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, smaller)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, smaller)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, smaller, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, larger)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, larger)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, larger, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, unchosen)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, unchosen, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void oneCreatureGetsTrampleWhenPowerDifferenceIsZero() {
        Permanent creature = addCreature(2);
        harness.setHand(player1, List.of(new SpryAndMighty()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addCreature(int power) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TestCards.mutableCard(creature).setPower(power);
        TestCards.mutableCard(creature).setToughness(power);
        return creature;
    }
}
