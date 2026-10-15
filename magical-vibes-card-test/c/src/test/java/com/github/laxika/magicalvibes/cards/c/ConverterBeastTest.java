package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConverterBeast.class})
class ConverterBeastTest extends BaseCardTest {

    @Test
    void entersWithAnIncubatorWithFiveCounters() {
        castConverterBeast();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void incubatorCanTransformForTwoMana() {
        castConverterBeast();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
    }

    @Test
    void incubatorEntersAsOneUntappedNoncreatureArtifactWithIncubatorSubtype() {
        castConverterBeast();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(incubator.isTapped()).isFalse();
        assertThat(incubator.getCard().getSubtypes()).extracting(Enum::name).contains("INCUBATOR");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void transformedTokenRetainsCountersAndBecomesFiveFivePhyrexianArtifactCreature() {
        castConverterBeast();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(5);
        assertThat(incubator.getCard().getSubtypes()).extracting(Enum::name).containsExactly("PHYREXIAN");
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
    }

    @Test
    void twoPendingTransformActivationsDoNotTransformTokenBack() {
        castConverterBeast();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, index, null, null);
        harness.activateAbility(player1, index, null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(5);
    }

    private void castConverterBeast() {
        harness.setHand(player1, List.of(new ConverterBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
