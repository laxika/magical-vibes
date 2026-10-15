package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RenegadeFreighter.class, DukharaPeafowl.class})
class RenegadeFreighterTest extends BaseCardTest {

    @Test
    void crewAnimatesFreighterAndTapsTheCrewedCreature() {
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        Permanent crew = addCreatureReady(player1, new DukharaPeafowl());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackingFreighterGetsPlusOnePlusOneAndTrampleUntilEndOfTurn() {
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        addCreatureReady(player1, new DukharaPeafowl());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, freighter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, freighter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, freighter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, freighter)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.isCreature(gd, freighter)).isFalse();
    }

    @Test
    void crewTapsSummoningSickCreatureBeforeAnimationResolves() {
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(freighter.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, freighter)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotCrewUsingTappedOrOpponentsCreatures() {
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        Permanent tappedCrew = addCreatureReady(player1, new DukharaPeafowl());
        tappedCrew.tap();
        addCreatureReady(player2, new DukharaPeafowl());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(gqs.isCreature(gd, freighter)).isFalse();
    }

    @Test
    void animatedFreighterCannotCrewItself() {
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        addCreatureReady(player1, new DukharaPeafowl());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(freighter.isTapped()).isFalse();
    }
}
