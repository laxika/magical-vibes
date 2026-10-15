package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrownyardAmalgam.class, Forest.class})
class DrownyardAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield mills three cards from the target player's library")
    void entersAndMillsTargetPlayer() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DrownyardAmalgam()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The activated ability makes Drownyard Amalgam unblockable until end of turn")
    void activatedAbilityMakesItUnblockableUntilEndOfTurn() {
        Permanent amalgam = addCreatureReady(player1, new DrownyardAmalgam());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(amalgam.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(amalgam.isCantBeBlocked()).isFalse();
    }

    @Test
    void enterTriggerCanMillItsController() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new DrownyardAmalgam()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
    }

    @Test
    void millsAllAvailableCardsFromAShortLibrary() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new DrownyardAmalgam()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void millingAnEmptyLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new DrownyardAmalgam()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Drownyard Amalgam");
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new DrownyardAmalgam());
        amalgam.setSummoningSick(true);
        amalgam.tap();
        Permanent other = addCreatureReady(player1, new DrownyardAmalgam());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(amalgam.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        assertThat(amalgam.isCantBeBlocked()).isTrue();
        assertThat(amalgam.isTapped()).isTrue();
        assertThat(other.isCantBeBlocked()).isFalse();
    }
}
