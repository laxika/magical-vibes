package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.t.ThievingMagpie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LandloreNavigator.class, Memnite.class, ThievingMagpie.class})
class LandloreNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Map token")
    void attackingCreatesMapToken() {
        addReadyNavigator();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("Two artifacts entering this turn conjures Thieving Magpie at your end step")
    void twoArtifactsConjureThievingMagpie() {
        addReadyNavigator();
        harness.setHand(player1, List.of(new Memnite(), new Memnite()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thieving Magpie")).hasSize(1);
    }

    @Test
    @DisplayName("Fewer than two artifacts entering this turn does not conjure Thieving Magpie")
    void fewerThanTwoArtifactsDoNotConjureThievingMagpie() {
        addReadyNavigator();
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thieving Magpie")).isEmpty();
    }

    private Permanent addReadyNavigator() {
        Permanent navigator = harness.addToBattlefieldAndReturn(player1, new LandloreNavigator());
        navigator.setSummoningSick(false);
        return navigator;
    }
}
