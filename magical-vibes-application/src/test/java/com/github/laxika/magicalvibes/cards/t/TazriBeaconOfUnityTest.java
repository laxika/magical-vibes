package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GeneralTazri;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TazriBeaconOfUnity.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class,
        CanopyBaloth.class, GeneralTazri.class, StoneworkPackbeast.class})
class TazriBeaconOfUnityTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces Tazri's generic casting cost by four")
    void fullPartyReducesCastingCost() {
        addFullParty();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TazriBeaconOfUnity(), "{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazri, Beacon of Unity");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tazri's ability offers at most two matching cards from the top six")
    void abilityOffersAtMostTwoMatchingCards() {
        harness.addToBattlefield(player1, new TazriBeaconOfUnity());
        SoulWarden cleric = new SoulWarden();
        FaerieMiscreant rogue = new FaerieMiscreant();
        GrizzlyBears nonmatching = new GrizzlyBears();
        FugitiveWizard wizard = new FugitiveWizard();
        BoggartBrute warrior = new BoggartBrute();
        SoulWarden matchingBelowTopSix = new SoulWarden();
        harness.setLibrary(player1, List.of(cleric, rogue, nonmatching, wizard, warrior,
                new GrizzlyBears(), matchingBelowTopSix));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                cleric.getId(), rogue.getId(), wizard.getId(), warrior.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).doesNotContain(matchingBelowTopSix.getId());
    }

    @Test
    @DisplayName("Tazri puts selected cards into hand and randomly bottoms the rest")
    void selectedCardsGoToHandAndRestGoToBottom() {
        harness.addToBattlefield(player1, new TazriBeaconOfUnity());
        SoulWarden cleric = new SoulWarden();
        FaerieMiscreant rogue = new FaerieMiscreant();
        GrizzlyBears nonmatching = new GrizzlyBears();
        FugitiveWizard wizard = new FugitiveWizard();
        BoggartBrute warrior = new BoggartBrute();
        GrizzlyBears secondNonmatching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(cleric, rogue, nonmatching, wizard, warrior,
                secondNonmatching));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(cleric.getId(), wizard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(cleric, wizard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(rogue, nonmatching, warrior, secondNonmatching);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }


    @Test
    @DisplayName("One creature with all party roles reduces the casting cost only once")
    void multitypeCreatureCountsOnlyOnce() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new TazriBeaconOfUnity(), "{3}{W}");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazri, Beacon of Unity");
    }

    @Test
    @DisplayName("Four multitype creatures form a full party")
    void multitypeCreaturesMaximizePartySize() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new TazriBeaconOfUnity(), "{W}");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazri, Beacon of Unity");
    }

    @Test
    @DisplayName("Opposing creatures and unrelated creature types do not reduce the cost")
    void opponentsPartyAndUnrelatedCreaturesDoNotCount() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new StoneworkPackbeast());
        }
        harness.addToBattlefield(player1, new CanopyBaloth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new TazriBeaconOfUnity(), "{4}{W}");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazri, Beacon of Unity");
    }

    @Test
    @DisplayName("The activated ability can be paid entirely with colored mana")
    void abilityAcceptsColoredManaAndEmptyLibrary() {
        harness.addToBattlefield(player1, new TazriBeaconOfUnity());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The activated ability accepts mixed generic and colored payments")
    void abilityAcceptsMixedPaymentWithNoMatchingCards() {
        harness.addToBattlefield(player1, new TazriBeaconOfUnity());
        CanopyBaloth first = new CanopyBaloth();
        CanopyBaloth second = new CanopyBaloth();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing no cards bottoms only the top six and preserves the untouched library")
    void mayChooseZeroCards() {
        harness.addToBattlefield(player1, new TazriBeaconOfUnity());
        List<TazriBeaconOfUnity> topSix = List.of(new TazriBeaconOfUnity(),
                new TazriBeaconOfUnity(), new TazriBeaconOfUnity(),
                new TazriBeaconOfUnity(), new TazriBeaconOfUnity(), new TazriBeaconOfUnity());
        CanopyBaloth untouched = new CanopyBaloth();
        ArrayList<Card> library = new ArrayList<>(topSix);
        library.add(untouched);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(topSix);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library allows choosing one Ally without any party role")
    void mayChooseOneAllyFromShortLibrary() {
        harness.addToBattlefield(player1, new TazriBeaconOfUnity());
        GeneralTazri ally = new GeneralTazri();
        CanopyBaloth nonmatching = new CanopyBaloth();
        TazriBeaconOfUnity warrior = new TazriBeaconOfUnity();
        harness.setLibrary(player1, List.of(ally, nonmatching, warrior));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ally.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ally);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonmatching, warrior);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Subtype defining abilities apply to cards in the library")
    void maySelectPackbeastFromLibrary() {
        harness.addToBattlefield(player1, new TazriBeaconOfUnity());
        StoneworkPackbeast packbeast = new StoneworkPackbeast();
        harness.setLibrary(player1, List.of(packbeast));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(packbeast.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(packbeast);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }
}
