package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.e.EchoingCalm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PulseOfTheDross.class, AuriokGlaivemaster.class, CrazedGoblin.class,
        DarksteelCitadel.class, EchoingCalm.class})
class PulseOfTheDrossTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen revealed card is discarded and the spell returns when the target has more cards")
    void discardsChosenCardAndReturnsWhenTargetHasMoreCards() {
        PulseOfTheDross pulse = new PulseOfTheDross();
        DarksteelCitadel citadel = new DarksteelCitadel();
        AuriokGlaivemaster glaivemaster = new AuriokGlaivemaster();
        EchoingCalm calm = new EchoingCalm();
        CrazedGoblin goblin = new CrazedGoblin();
        harness.setHand(player1, List.of(pulse, citadel));
        harness.setHand(player2, List.of(glaivemaster, calm, goblin));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealCardsDiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(citadel, pulse);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(calm);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(glaivemaster, goblin);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
    }

    @Test
    @DisplayName("The spell goes to the graveyard when the target does not have more cards after discarding")
    void goesToGraveyardWhenTargetDoesNotHaveMoreCards() {
        PulseOfTheDross pulse = new PulseOfTheDross();
        DarksteelCitadel citadel = new DarksteelCitadel();
        AuriokGlaivemaster glaivemaster = new AuriokGlaivemaster();
        CrazedGoblin goblin = new CrazedGoblin();
        harness.setHand(player1, List.of(pulse, citadel));
        harness.setHand(player2, List.of(glaivemaster, goblin));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(citadel);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pulse);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(goblin);
    }

    @Test
    @DisplayName("The target chooses three cards when their hand has more than three")
    void targetChoosesThreeCardsFromLargerHand() {
        PulseOfTheDross pulse = new PulseOfTheDross();
        DarksteelCitadel citadel = new DarksteelCitadel();
        AuriokGlaivemaster glaivemaster = new AuriokGlaivemaster();
        EchoingCalm calm = new EchoingCalm();
        CrazedGoblin goblin = new CrazedGoblin();
        DarksteelCitadel discardedCitadel = new DarksteelCitadel();
        harness.setHand(player1, List.of(pulse, citadel));
        harness.setHand(player2, List.of(glaivemaster, calm, goblin, discardedCitadel));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(3);

        harness.handleCardChosen(player2, 3);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(citadel, pulse);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discardedCitadel);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(glaivemaster, calm, goblin);
    }

    @Test
    @DisplayName("Targeting yourself does not return the spell because the hand sizes cannot differ")
    void targetingYourselfDoesNotReturnTheSpell() {
        PulseOfTheDross pulse = new PulseOfTheDross();
        DarksteelCitadel citadel = new DarksteelCitadel();
        AuriokGlaivemaster glaivemaster = new AuriokGlaivemaster();
        harness.setHand(player1, List.of(pulse, citadel, glaivemaster));
        addMana();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(glaivemaster);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pulse, citadel);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
