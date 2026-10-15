package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.t.ThievingMagpie;
import com.github.laxika.magicalvibes.model.TurnStep;
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
        addCreatureReady(player1, new LandloreNavigator());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("Two artifacts entering this turn conjures Thieving Magpie at your end step")
    void twoArtifactsConjureThievingMagpie() {
        addCreatureReady(player1, new LandloreNavigator());
        harness.setHand(player1, List.of(new Memnite(), new Memnite()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thieving Magpie")).hasSize(1);
    }

    @Test
    @DisplayName("Fewer than two artifacts entering this turn does not conjure Thieving Magpie")
    void fewerThanTwoArtifactsDoNotConjureThievingMagpie() {
        addCreatureReady(player1, new LandloreNavigator());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thieving Magpie")).isEmpty();
    }

    @Test
    @DisplayName("Artifacts entering under an opponent's control do not count")
    void opponentsArtifactsDoNotCount() {
        addCreatureReady(player1, new LandloreNavigator());
        harness.enterBattlefieldAndReturn(player1, new Memnite());
        harness.enterBattlefieldAndReturn(player2, new Memnite());
        harness.enterBattlefieldAndReturn(player2, new Memnite());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Thieving Magpie")).isEmpty();
    }

    @Test
    @DisplayName("Nonartifact entries do not satisfy the end-step condition")
    void nonartifactEntriesDoNotCount() {
        harness.enterBattlefieldAndReturn(player1, new LandloreNavigator());
        harness.enterBattlefieldAndReturn(player1, new ThievingMagpie());
        harness.enterBattlefieldAndReturn(player1, new Memnite());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Thieving Magpie")).hasSize(1);
    }

    @Test
    @DisplayName("Map tokens created by attacking satisfy the artifact-entry condition")
    void mapTokensCountAsArtifactEntries() {
        addCreatureReady(player1, new LandloreNavigator());
        addCreatureReady(player1, new LandloreNavigator());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Map")).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thieving Magpie")).hasSize(2)
                .allSatisfy(magpie -> {
                    assertThat(magpie.getCard().isToken()).isFalse();
                    assertThat(magpie.getCard().getOwnerId()).isEqualTo(player1.getId());
                });
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's end step")
    void opponentsEndStepDoesNotTrigger() {
        addCreatureReady(player1, new LandloreNavigator());
        harness.enterBattlefieldAndReturn(player1, new Memnite());
        harness.enterBattlefieldAndReturn(player1, new Memnite());
        harness.forceActivePlayer(player2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Thieving Magpie")).isEmpty();
    }
}
