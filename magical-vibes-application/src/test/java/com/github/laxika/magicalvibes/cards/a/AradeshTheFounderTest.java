package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AradeshTheFounder.class, CentaurCourser.class, GrizzlyBears.class})
class AradeshTheFounderTest extends BaseCardTest {

    @Test
    void enlistingGrantsDoubleStrikeAndDrawsAtPowerThreshold() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aradesh)));
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(aradesh.getPowerModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void enlistingBelowPowerThresholdStillGrantsDoubleStrikeWithoutDrawing() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aradesh)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(aradesh.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void doesNotTriggerWhenTheAttackerDoesNotEnlist() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }
}
