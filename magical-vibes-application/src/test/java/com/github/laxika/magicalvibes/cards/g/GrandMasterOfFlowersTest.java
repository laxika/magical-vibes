package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwarfholdChampion;
import com.github.laxika.magicalvibes.cards.l.LoyalWarhound;
import com.github.laxika.magicalvibes.cards.m.MonkOfTheOpenHand;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrandMasterOfFlowers.class, DwarfholdChampion.class, MonkOfTheOpenHand.class,
        LoyalWarhound.class, PsychogenicProbe.class})
class GrandMasterOfFlowersTest extends BaseCardTest {

    @Test
    @DisplayName("At seven loyalty, Grand Master becomes a 7/7 Dragon God creature")
    void becomesDragonGodCreatureAtSevenLoyalty() {
        Permanent grandMaster = addReadyGrandMaster(player1, 6);

        assertThat(gqs.isCreature(gd, grandMaster)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, grandMaster)).isTrue();

        grandMaster.setCounterCount(CounterType.LOYALTY, 7);

        assertThat(gqs.isCreature(gd, grandMaster)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, grandMaster)).isFalse();
        assertThat(gqs.getEffectivePower(gd, grandMaster)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, grandMaster)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, grandMaster))
                .contains(CardSubtype.DRAGON, CardSubtype.GOD);
        assertThat(gqs.hasKeyword(gd, grandMaster, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, grandMaster, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The first +1 locks a qualifying creature until the controller's next turn")
    void firstPlusOneLocksCreature() {
        addReadyGrandMaster(player1, 5);
        Permanent target = addCreatureReady(player2, new DwarfholdChampion());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
    }

    @Test
    @DisplayName("The second +1 finds Monk of the Open Hand in the library")
    void secondPlusOneFindsMonkInLibrary() {
        addReadyGrandMaster(player1, 5);
        Card monk = new MonkOfTheOpenHand();
        harness.setLibrary(player1, List.of(monk));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(monk.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(monk);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(monk);
    }

    @Test
    void revertsWhenLoyaltyFallsBelowSeven() {
        Permanent grandMaster = addReadyGrandMaster(player1, 8);
        assertThat(gqs.isCreature(gd, grandMaster)).isTrue();

        grandMaster.setCounterCount(CounterType.LOYALTY, 6);

        assertThat(gqs.isCreature(gd, grandMaster)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, grandMaster)).isTrue();
        assertThat(gqs.hasKeyword(gd, grandMaster, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, grandMaster, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void addingSeventhLoyaltyAnimatesBeforeAbilityResolves() {
        Permanent grandMaster = addReadyGrandMaster(player1, 6);
        Permanent target = addCreatureReady(player2, new DwarfholdChampion());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, grandMaster)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, grandMaster)).isFalse();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
    }

    @Test
    void canActivateLoyaltyAbilityWhileCreatureButOnlyOncePerTurn() {
        Permanent grandMaster = addReadyGrandMaster(player1, 7);
        Permanent target = addCreatureReady(player2, new DwarfholdChampion());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(grandMaster.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {"FIRST_STRIKE", "DOUBLE_STRIKE", "VIGILANCE"})
    void cannotTargetCreatureWithExcludedKeyword(Keyword keyword) {
        Permanent grandMaster = addReadyGrandMaster(player1, 5);
        Permanent target = addCreatureReady(player2, new DwarfholdChampion());
        target.getPersistentGrantedKeywords().add(keyword);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(grandMaster.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetCreatureWithPrintedVigilance() {
        addReadyGrandMaster(player1, 5);
        Permanent target = addCreatureReady(player2, new LoyalWarhound());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetGainingVigilanceBeforeResolutionIsNotLocked() {
        addReadyGrandMaster(player1, 5);
        Permanent target = addCreatureReady(player2, new DwarfholdChampion());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        target.getPersistentGrantedKeywords().add(Keyword.VIGILANCE);

        harness.passBothPriorities();

        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    @Test
    void lockExpiresAtControllersNextTurn() {
        addReadyGrandMaster(player1, 5);
        Permanent target = addCreatureReady(player2, new DwarfholdChampion());
        harness.setLibrary(player2, List.of(new DwarfholdChampion(), new MonkOfTheOpenHand()));
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isFalse();
    }

    @Test
    void secondPlusOneFindsMonkInGraveyard() {
        addReadyGrandMaster(player1, 5);
        Card monk = new MonkOfTheOpenHand();
        harness.setGraveyard(player1, List.of(monk));
        harness.setLibrary(player1, List.of(new DwarfholdChampion()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(monk.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(monk);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(monk);
    }

    @Test
    void searchCanResolveWithoutAnyMonk() {
        Permanent grandMaster = addReadyGrandMaster(player1, 5);
        harness.setLibrary(player1, List.of(new DwarfholdChampion()));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(grandMaster.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void retrievesOnlyOneMonkFromCombinedZones() {
        addReadyGrandMaster(player1, 5);
        Card libraryMonk = new MonkOfTheOpenHand();
        Card graveyardMonk = new MonkOfTheOpenHand();
        harness.setLibrary(player1, List.of(libraryMonk));
        harness.setGraveyard(player1, List.of(graveyardMonk));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(libraryMonk.getId(), graveyardMonk.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(libraryMonk.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(libraryMonk).doesNotContain(graveyardMonk);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardMonk);
    }

    @Test
    void graveyardOnlySearchDoesNotCauseShuffleDamage() {
        addReadyGrandMaster(player1, 5);
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Card monk = new MonkOfTheOpenHand();
        harness.setGraveyard(player1, List.of(monk));
        harness.setLibrary(player1, List.of(new DwarfholdChampion()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(monk.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(monk);
        harness.assertLife(player1, lifeBefore);
    }

    private Permanent addReadyGrandMaster(Player player, int loyalty) {
        Permanent permanent = addCreatureReady(player, new GrandMasterOfFlowers());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
