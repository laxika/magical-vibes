package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.Gravkill;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FamishedWorldsire.class, FrenziedBaloth.class, Mountain.class, Gravkill.class,
        PsychogenicProbe.class})
class FamishedWorldsireTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed lands add counters and power determines the revealed cards")
    void sacrificedLandsAddCountersAndPowerDeterminesRevealCount() {
        Permanent sacrificedMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent sacrificedMountainTwo = harness.addToBattlefieldAndReturn(player1, new Mountain());
        List<Card> library = new ArrayList<>(List.of(
                new Mountain(), new FrenziedBaloth(), new FrenziedBaloth(),
                new FrenziedBaloth(), new Mountain(), new FrenziedBaloth(), new FrenziedBaloth()));
        harness.setLibrary(player1, library);

        castWorldsire();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(sacrificedMountain.getId(), sacrificedMountainTwo.getId()));

        Permanent worldsire = findPermanent(player1, "Famished Worldsire");
        assertThat(worldsire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactly(
                library.get(0).getId(), library.get(4).getId());
        assertThat(choice.selectedToBattlefieldTapped()).isTrue();

        harness.handleMultipleCardsChosen(player1, choice.validCardIds());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                library.get(1), library.get(2), library.get(3), library.get(5), library.get(6));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().equals(library.get(0))
                        || permanent.getCard().equals(library.get(4))))
                .hasSize(2).allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Choosing no lands leaves the lands and makes the 0/0 die")
    void choosingNoLandsLeavesTheLandsAndWorldsireDies() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castWorldsire();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(countPermanents(player1, "Famished Worldsire")).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(
                mountain);
    }

    @Test
    void devourOffersOnlyLandsControlledByTheController() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FrenziedBaloth());
        harness.setLibrary(player1, List.of(new FrenziedBaloth()));

        castWorldsire();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownLand.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(findPermanent(player1, "Famished Worldsire")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void powerIsDeterminedAtResolutionAndOnlySomeLandsMayBeChosen() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        List<Card> library = List.of(new Mountain(), new Mountain(), new FrenziedBaloth(),
                new Mountain(), new FrenziedBaloth());
        harness.setLibrary(player1, library);

        castWorldsire();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        findPermanent(player1, "Famished Worldsire").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(library.subList(0, 4));
        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(
                library.subList(1, 5));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(library.get(0).getId())))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void decliningAllRevealedLandsStillShuffles() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        List<Card> library = List.of(new Mountain(), new Mountain(), new FrenziedBaloth());
        harness.setLibrary(player1, library);
        harness.setLife(player1, 20);

        castWorldsire();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(countPermanents(player1, "Mountain")).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    void zeroPowerStillShufflesAfterWorldsireDies() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        List<Card> library = List.of(new Mountain(), new FrenziedBaloth());
        harness.setLibrary(player1, library);
        harness.setLife(player1, 20);

        castWorldsire();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.assertInGraveyard(player1, "Famished Worldsire");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        harness.assertLife(player1, 18);
    }

    @Test
    void emptyLibraryStillShuffles() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        castWorldsire();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Famished Worldsire");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    void noAvailableLandsStillCreatesTheEntersTrigger() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        castWorldsire();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Famished Worldsire");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
    }

    @Test
    void removingWorldsireInResponseUsesItsLastKnownPower() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        List<Card> library = List.of(new Mountain(), new FrenziedBaloth(),
                new Mountain(), new FrenziedBaloth());
        harness.setLibrary(player1, library);

        castWorldsire();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        Permanent worldsire = findPermanent(player1, "Famished Worldsire");
        harness.setHand(player1, List.of(new Gravkill()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, worldsire.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Famished Worldsire");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(library.subList(0, 3));
        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId(), library.get(2).getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                library.get(1), library.get(3));
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(2);
    }

    @Test
    void wardCountersOpposingRemovalWhenPaymentIsDeclined() {
        Permanent worldsire = harness.addToBattlefieldAndReturn(player1, new FamishedWorldsire());
        worldsire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player2, List.of(new Gravkill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.castInstant(player2, 0, worldsire.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Famished Worldsire");
        harness.assertInGraveyard(player2, "Gravkill");
    }

    @Test
    void payingThreeManaForWardAllowsOpposingRemoval() {
        Permanent worldsire = harness.addToBattlefieldAndReturn(player1, new FamishedWorldsire());
        worldsire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player2, List.of(new Gravkill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.castInstant(player2, 0, worldsire.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Famished Worldsire");
        assertThat(gd.exiledCards).anySatisfy(exiled ->
                assertThat(exiled.card().getId()).isEqualTo(worldsire.getCard().getId()));
    }

    private void castWorldsire() {
        harness.castFromHand(player1, new FamishedWorldsire(), "{5}{G}{G}{G}");
    }
}
