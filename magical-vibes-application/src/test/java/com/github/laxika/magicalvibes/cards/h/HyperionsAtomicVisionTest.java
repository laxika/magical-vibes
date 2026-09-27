package com.github.laxika.magicalvibes.cards.h;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HyperionsAtomicVision.class, HyperionSupremeHero.class, GrizzlyBears.class})
class HyperionsAtomicVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature and scries 2 when a Hero permanent is beheld")
    void beheldHeroPermanentDestroysAndScries() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HyperionSupremeHero());
        Permanent target = tappedCreature();
        Card topCard = new GrizzlyBears();
        Card secondCard = new HyperionSupremeHero();
        setLibrary(topCard, secondCard);
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(hero.getId()), List.of());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a tapped creature and scries 2 when a Hero card is beheld from hand")
    void beheldHeroCardDestroysAndScries() {
        Permanent target = tappedCreature();
        Card topCard = new GrizzlyBears();
        Card secondCard = new HyperionSupremeHero();
        setLibrary(topCard, secondCard);
        harness.setHand(player1, List.of(new HyperionsAtomicVision(), new HyperionSupremeHero()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not scry when the optional Hero behold is declined")
    void declinedBeholdOmitsScry() {
        Permanent target = tappedCreature();
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Rejects an untapped creature as the target")
    void rejectsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    private Permanent tappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        return target;
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
