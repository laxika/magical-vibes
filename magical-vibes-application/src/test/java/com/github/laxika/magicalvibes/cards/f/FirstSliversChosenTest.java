package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstSliversChosen.class, MetallicSliver.class, GrizzlyBears.class})
class FirstSliversChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Slivers you control get exalted when attacking alone")
    void sliverAttackingAloneGetsExaltedBoost() {
        addCreatureReady(player1, new FirstSliversChosen());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not get exalted")
    void nonSliverDoesNotGetExaltedBoost() {
        addCreatureReady(player1, new FirstSliversChosen());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple First Sliver's Chosen grant multiple exalted instances")
    void multipleSourcesGrantMultipleExaltedInstances() {
        addCreatureReady(player1, new FirstSliversChosen());
        addCreatureReady(player1, new FirstSliversChosen());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(7);
    }
}
