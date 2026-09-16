package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalTherapist.class, GrizzlyBears.class, Shock.class, Forest.class})
class CabalTherapistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature names a nonland card and discards all matching cards")
    void sacrificesCreatureAndDiscardsMatchingCards() {
        addCabalTherapist();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> targetHand = new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Shock(), new Forest()));
        harness.setHand(player2, targetHand);

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Grizzly Bears", "Shock").doesNotContain("Forest");
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(targetHand.get(2), targetHand.get(3));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(targetHand.get(0), targetHand.get(1));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the sacrifice does not discard from the target's hand")
    void decliningSacrificeDoesNothing() {
        addCabalTherapist();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock()));

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The ability does nothing when no creature can be sacrificed")
    void noCreatureToSacrificeDoesNothing() {
        Permanent therapist = harness.addToBattlefieldAndReturn(player1, new CabalTherapist());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock()));

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(therapist);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addCabalTherapist() {
        harness.addToBattlefield(player1, new CabalTherapist());
    }

    private void advanceToFirstMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
