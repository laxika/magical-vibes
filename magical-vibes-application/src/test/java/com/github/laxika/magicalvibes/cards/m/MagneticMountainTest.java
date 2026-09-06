package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagneticMountain.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class})
class MagneticMountainTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped blue creature stays tapped through the untap step while a non-blue one untaps")
    void blueCreatureStaysTappedWhileGreenUntaps() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent merfolk = addTapped(player1, new MerfolkOfThePearlTrident());
        Permanent bears = addTapped(player1, new GrizzlyBears());     // Green 2/2

        advanceToUpkeep(player1);

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {4} untaps the chosen blue creature")
    void payingFourUntapsChosenBlueCreature() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent merfolk = addTapped(player1, new MerfolkOfThePearlTrident());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities(); // resolve the trigger -> begins the multi-permanent choice
        harness.handleMultiplePermanentsChosen(player1, List.of(merfolk.getId()));

        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {8} untaps two chosen blue creatures")
    void payingEightUntapsTwoBlueCreatures() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent merfolkA = addTapped(player1, new MerfolkOfThePearlTrident());
        Permanent merfolkB = addTapped(player1, new MerfolkOfThePearlTrident());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(merfolkA.getId(), merfolkB.getId()));

        assertThat(merfolkA.isTapped()).isFalse();
        assertThat(merfolkB.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With only {3} available, no creature can be untapped (cost of {4} not met)")
    void insufficientManaLeavesBlueCreatureTapped() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent merfolk = addTapped(player1, new MerfolkOfThePearlTrident());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities(); // trigger resolves as a no-op — can't afford any creature

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the blue creature tapped")
    void choosingNoneLeavesBlueCreatureTapped() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent merfolk = addTapped(player1, new MerfolkOfThePearlTrident());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(merfolk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("During an opponent's upkeep, only that player's tapped blue creatures can be chosen")
    void opponentUpkeepUsesTheirOwnBlueCreaturesAndMana() {
        harness.addToBattlefield(player1, new MagneticMountain());
        Permanent ownMerfolk = addTapped(player1, new MerfolkOfThePearlTrident());
        Permanent opponentMerfolk = addTapped(player2, new MerfolkOfThePearlTrident());

        advanceToUpkeep(player2);
        assertThat(opponentMerfolk.isTapped()).isTrue();

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(opponentMerfolk.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentMerfolk.getId()));

        assertThat(opponentMerfolk.isTapped()).isFalse();
        assertThat(ownMerfolk.isTapped()).isTrue();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

}
