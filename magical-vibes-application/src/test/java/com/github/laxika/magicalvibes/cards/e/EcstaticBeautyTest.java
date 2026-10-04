package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.ProfaneTutor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EcstaticBeauty.class, ProfaneTutor.class, GrizzlyBears.class, ClockworkDroid.class, Mountain.class})
class EcstaticBeautyTest extends BaseCardTest {

    @Test
    void exilesTopThreeAndSuspendsOnlyExiledCardsWithSuspend() {
        ProfaneTutor alreadySuspended = suspendProfaneTutor();
        ProfaneTutor newlyExiledSuspended = new ProfaneTutor();
        GrizzlyBears nonSuspended = new GrizzlyBears();
        GrizzlyBears anotherNonSuspended = new GrizzlyBears();
        harness.setLibrary(player1, List.of(newlyExiledSuspended, nonSuspended, anotherNonSuspended));

        castBeauty();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(newlyExiledSuspended, nonSuspended, anotherNonSuspended);
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(alreadySuspended.getId(), 2)
                .containsEntry(newlyExiledSuspended.getId(), 4)
                .doesNotContainKeys(nonSuspended.getId(), anotherNonSuspended.getId());
        for (Card card : List.of(newlyExiledSuspended, nonSuspended, anotherNonSuspended)) {
            assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(card.getId());
        }
    }

    @Test
    void suspendsFromHandWithFourTimeCounters() {
        EcstaticBeauty card = new EcstaticBeauty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    private ProfaneTutor suspendProfaneTutor() {
        ProfaneTutor card = new ProfaneTutor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void castBeauty() {
        harness.castFromHand(player1, new EcstaticBeauty(), "{2}{R}");
        harness.passBothPriorities();
    }

    @Test
    void mayCastExiledCreatureByPayingItsNormalCost() {
        ClockworkDroid card = new ClockworkDroid();
        harness.setLibrary(player1, List.of(card));
        castBeauty();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Clockwork Droid");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    void mayPlayExiledLand() {
        Mountain land = new Mountain();
        harness.setLibrary(player1, List.of(land));
        castBeauty();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
    }

    @Test
    void shortLibraryExilesOnlyAvailableCardsAndSuspendsBeauty() {
        EcstaticBeauty card = new EcstaticBeauty();
        harness.setLibrary(player1, List.of(card));

        castBeauty();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void emptyLibraryStillResolvesNormally() {
        harness.setLibrary(player1, List.of());

        castBeauty();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ecstatic Beauty");
    }

    @Test
    void temporaryPermissionExpiresButSuspendCountersRemain() {
        EcstaticBeauty card = new EcstaticBeauty();
        harness.setLibrary(player1, List.of(card));
        castBeauty();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void suspendIsSpecialActionAndRequiresSorceryTiming() {
        EcstaticBeauty card = new EcstaticBeauty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void fourthOwnersUpkeepAllowsFreeCast() {
        EcstaticBeauty card = suspendThroughFourUpkeeps();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        harness.assertInGraveyard(player1, "Ecstatic Beauty");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .hasSize(3).allMatch(exiled -> exiled instanceof ClockworkDroid);
    }

    @Test
    void decliningFreeCastLeavesBeautyExiledWithoutTimeCounters() {
        EcstaticBeauty card = suspendThroughFourUpkeeps();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotInGraveyard(player1, "Ecstatic Beauty");
    }

    private EcstaticBeauty suspendThroughFourUpkeeps() {
        EcstaticBeauty card = new EcstaticBeauty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.setLibrary(player1, List.of(
                new ClockworkDroid(), new ClockworkDroid(), new ClockworkDroid()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);

        for (int remaining = 3; remaining >= 0; remaining--) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            if (remaining > 0) {
                assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), remaining);
            } else {
                assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
            }
        }
        return card;
    }
}
