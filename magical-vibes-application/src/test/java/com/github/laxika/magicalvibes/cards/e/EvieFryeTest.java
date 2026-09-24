package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvieFrye.class, Forest.class, GrizzlyBears.class})
class EvieFryeTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the targeted player search for Jacob Frye")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card jacob = new Card();
        jacob.setName("Jacob Frye");
        harness.setLibrary(player2, List.of(jacob));
        harness.setHand(player1, List.of(new EvieFrye()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player2.getId())).contains(jacob);
    }

    @Test
    @DisplayName("Discarding a creature lets the controller choose one of their creatures to unblock")
    void creatureDiscardMakesOwnCreatureUnblockable() {
        addCreatureReady(player1, new EvieFrye());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(ownCreature.getId())
                .doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBeBlocked()).isTrue();
        assertThat(opposingCreature.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Discarding a noncreature does not create the unblockable trigger")
    void nonCreatureDiscardDoesNotTrigger() {
        addCreatureReady(player1, new EvieFrye());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBeBlocked()).isFalse();
    }
}
