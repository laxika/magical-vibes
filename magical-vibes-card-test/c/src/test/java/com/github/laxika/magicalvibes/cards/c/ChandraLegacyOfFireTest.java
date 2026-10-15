package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraLegacyOfFire.class, NicolBolasPlaneswalker.class, Mountain.class})
class ChandraLegacyOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of your end step deals damage for your planeswalkers")
    void endStepDamageCountsYourPlaneswalkers() {
        addReadyChandra(player1, 4);
        addReadyPlaneswalker(player1, 3);
        addReadyPlaneswalker(player2, 3);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("+1 adds red mana for each planeswalker you control")
    void plusOneAddsManaForControlledPlaneswalkers() {
        addReadyChandra(player1, 4);
        addReadyPlaneswalker(player1, 3);
        addReadyPlaneswalker(player2, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("0 removes loyalty counters, exiles that many cards, and grants play permission")
    void zeroRemovesCountersAndExilesMatchingNumberOfCards() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent nicol = addReadyPlaneswalker(player1, 3);
        Card mountain = new Mountain();
        Card secondCard = new Mountain();
        harness.setLibrary(player1, List.of(mountain, secondCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(chandra.getId(), nicol.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(chandra.getId(), nicol.getId()));

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(nicol.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(mountain, secondCard);
        assertThat(gd.exilePlayPermissions).containsEntry(mountain.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(secondCard.getId(), player1.getId());
    }

    @Test
    void plusOneUsesTheStackAndCountsAtResolution() {
        Permanent chandra = addReadyChandra(player1, 3);
        Permanent other = addReadyPlaneswalker(player1, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(other);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void endStepCountsAtResolutionEvenAfterChandraLeaves() {
        Permanent chandra = addReadyChandra(player1, 3);
        addReadyPlaneswalker(player1, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        gd.playerBattlefields.get(player1.getId()).remove(chandra);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addReadyChandra(player1, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void zeroCanChooseNoPermanents() {
        Permanent chandra = addReadyChandra(player1, 3);
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void zeroCountsOnlyCountersActuallyRemovedIncludingFromNonplaneswalkers() {
        Permanent chandra = addReadyChandra(player1, 3);
        Permanent landWithLoyalty = harness.addToBattlefieldAndReturn(player1, new Mountain());
        landWithLoyalty.setCounterCount(CounterType.LOYALTY, 2);
        Permanent landWithoutLoyalty = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingPlaneswalker = addReadyPlaneswalker(player2, 3);
        Card first = new Mountain();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(landWithLoyalty.getId(), landWithoutLoyalty.getId())
                .doesNotContain(opposingPlaneswalker.getId());
        harness.handleMultiplePermanentsChosen(player1,
                List.of(landWithLoyalty.getId(), landWithoutLoyalty.getId()));

        assertThat(landWithLoyalty.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void zeroCanRemoveChandrasLastCounterAndExileFromAShortLibrary() {
        Permanent chandra = addReadyChandra(player1, 1);
        Permanent other = addReadyPlaneswalker(player1, 3);
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chandra.getId(), other.getId()));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chandra);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chandra.getCard());
        assertThat(other.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(mountain);
    }

    @Test
    void exiledLandsCanBePlayedButStillRespectTheLandPlayLimit() {
        Permanent chandra = addReadyChandra(player1, 3);
        Permanent other = addReadyPlaneswalker(player1, 3);
        Card first = new Mountain();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chandra.getId(), other.getId()));

        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exiledSpellStillRequiresManaAndNormalTiming() {
        Permanent chandra = addReadyChandra(player1, 3);
        Card exiledChandra = new ChandraLegacyOfFire();
        harness.setLibrary(player1, List.of(exiledChandra));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chandra.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledChandra.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledChandra);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.castFromExile(player1, exiledChandra.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, exiledChandra.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void unusedExiledCardRemainsExiledWhenPermissionExpires() {
        Permanent chandra = addReadyChandra(player1, 3);
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chandra.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(mountain);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(mountain.getId());
        harness.forceActivePlayer(player1);
        assertThatThrownBy(() -> harness.castFromExile(player1, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent chandra = harness.addToBattlefieldAndReturn(player, new ChandraLegacyOfFire());
        chandra.setCounterCount(CounterType.LOYALTY, loyalty);
        chandra.setSummoningSick(false);
        return chandra;
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }
}
