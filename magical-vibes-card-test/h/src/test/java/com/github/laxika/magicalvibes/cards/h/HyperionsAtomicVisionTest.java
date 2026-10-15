package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HyperionsAtomicVision.class, HyperionSupremeHero.class})
class HyperionsAtomicVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature and scries 2 when a Hero permanent is beheld")
    void beheldHeroPermanentDestroysAndScries() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HyperionSupremeHero());
        Permanent target = tappedCreature();
        Card topCard = new HyperionSupremeHero();
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

        harness.assertInGraveyard(player2, "Hyperion, Supreme Hero");
    }

    @Test
    @DisplayName("Destroys a tapped creature and scries 2 when a Hero card is beheld from hand")
    void beheldHeroCardDestroysAndScries() {
        Permanent target = tappedCreature();
        Card topCard = new HyperionSupremeHero();
        Card secondCard = new HyperionSupremeHero();
        setLibrary(topCard, secondCard);
        harness.setHand(player1, List.of(new HyperionsAtomicVision(), new HyperionSupremeHero()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.assertInGraveyard(player2, "Hyperion, Supreme Hero");
    }

    @Test
    @DisplayName("Does not scry when the optional Hero behold is declined")
    void declinedBeholdOmitsScry() {
        Permanent target = tappedCreature();
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Hyperion, Supreme Hero");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Rejects an untapped creature as the target")
    void rejectsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HyperionSupremeHero());
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    void untappedTargetOnResolutionPreventsDestructionAndScry() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HyperionSupremeHero());
        Permanent target = tappedCreature();
        Card topCard = new HyperionsAtomicVision();
        setLibrary(topCard, new HyperionSupremeHero());
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(hero.getId()), List.of());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hyperion, Supreme Hero");
        harness.assertInGraveyard(player1, "Hyperion's Atomic Vision");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void scryStillHappensWhenBeheldHeroLeavesBeforeResolution() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HyperionSupremeHero());
        Permanent target = tappedCreature();
        Card topCard = new HyperionsAtomicVision();
        Card secondCard = new HyperionSupremeHero();
        setLibrary(topCard, secondCard);
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(hero.getId()), List.of());
        gd.playerBattlefields.get(player1.getId()).remove(hero);
        gd.playerGraveyards.get(player1.getId()).add(hero.getCard());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        harness.assertInGraveyard(player2, "Hyperion, Supreme Hero");
    }

    @Test
    void cannotBeholdOpponentsHero() {
        Permanent target = tappedCreature();
        harness.setHand(player1, List.of(new HyperionsAtomicVision()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, target.getId(),
                List.of(target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeholdNonHeroCardFromHand() {
        Permanent target = tappedCreature();
        harness.setHand(player1, List.of(new HyperionsAtomicVision(), new HyperionsAtomicVision()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, target.getId(),
                List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeholdMoreThanOneHero() {
        Permanent target = tappedCreature();
        harness.setHand(player1, List.of(new HyperionsAtomicVision(),
                new HyperionSupremeHero(), new HyperionSupremeHero()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, target.getId(),
                List.of(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void beheldHandCardIsRevealedToBothPlayersDuringCasting() throws Exception {
        Permanent target = tappedCreature();
        Card hero = new HyperionSupremeHero();
        harness.setHand(player1, List.of(new HyperionsAtomicVision(), hero));
        addMana();
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hero);
        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player1.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(hero.getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    private Permanent tappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HyperionSupremeHero());
        target.tap();
        return target;
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
