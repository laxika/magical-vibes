package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProtectionRacket.class, SakuraTribeElder.class, Swamp.class, EverybodyLives.class})
class ProtectionRacketTest extends BaseCardTest {

    @Test
    void opponentPaysManaValueToExileRevealedCard() {
        SakuraTribeElder elder = new SakuraTribeElder();
        prepareUpkeep(elder);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elder);
        harness.assertNotInHand(player1, "Sakura-Tribe Elder");
    }

    @Test
    void decliningPaymentPutsRevealedCardIntoHand() {
        SakuraTribeElder elder = new SakuraTribeElder();
        prepareUpkeep(elder);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Sakura-Tribe Elder");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(elder);
    }

    @Test
    void cannotPayAutomaticallyPutsRevealedCardIntoHand() {
        SakuraTribeElder elder = new SakuraTribeElder();
        harness.setLife(player2, 1);
        prepareUpkeep(elder);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Sakura-Tribe Elder");
        harness.assertLife(player2, 1);
    }

    @Test
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new ProtectionRacket());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    void payingZeroLifeExilesRevealedLand() {
        Swamp swamp = new Swamp();
        prepareUpkeep(swamp);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(swamp);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInHand(player1, "Swamp");
    }

    @Test
    void decliningZeroLifePaymentPutsLandIntoHand() {
        Swamp swamp = new Swamp();
        prepareUpkeep(swamp);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Swamp");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(swamp);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        SakuraTribeElder elder = new SakuraTribeElder();
        harness.addToBattlefield(player1, new ProtectionRacket());
        harness.setLibrary(player1, List.of(elder));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elder);
        harness.assertNotInHand(player1, "Sakura-Tribe Elder");
    }

    @Test
    void opponentWhoCannotLoseLifeCannotPayPositiveManaValue() {
        SakuraTribeElder elder = new SakuraTribeElder();
        harness.addToBattlefield(player1, new ProtectionRacket());
        harness.setLibrary(player1, List.of(elder));
        advanceToUpkeep(player1);

        harness.castFromHand(player2, new EverybodyLives(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInHand(player1, "Sakura-Tribe Elder");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(elder);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentWhoCannotLoseLifeCanStillPayZero() {
        Swamp swamp = new Swamp();
        harness.addToBattlefield(player1, new ProtectionRacket());
        harness.setLibrary(player1, List.of(swamp));
        advanceToUpkeep(player1);

        harness.castFromHand(player2, new EverybodyLives(), "{1}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(swamp);
        harness.assertNotInHand(player1, "Swamp");
    }

    private void prepareUpkeep(Card topCard) {
        harness.addToBattlefield(player1, new ProtectionRacket());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();
    }
}
