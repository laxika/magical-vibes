package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TazriBeaconOfUnity.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class})
class TazriBeaconOfUnityTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces Tazri's generic casting cost by four")
    void fullPartyReducesCastingCost() {
        addFullParty();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TazriBeaconOfUnity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
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

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }
}
