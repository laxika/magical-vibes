package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGrandTour.class, GrizzlyBears.class, Shock.class, ObstinateBaloth.class, Pacifism.class})
class TheGrandTourTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a permanent, makes its owner discard it, and returns it to the battlefield")
    void exilesDiscardsAndReturnsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card unrelated = new Shock();
        harness.setHand(player2, List.of(unrelated));
        harness.setHand(player1, List.of(new TheGrandTour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(unrelated.getId(), target.getCard().getId());

        harness.handleCardChosen(player2, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactly(target.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .containsExactly(unrelated.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The returned permanent is a new object without its previous counters or damage")
    void returnsNewPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setMarkedDamage(1);
        target.tap();
        harness.setHand(player1, List.of(new TheGrandTour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCard().getId()).isEqualTo(target.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A token stops in exile and never returns to hand or the battlefield")
    void tokenDoesNotReturn() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, token);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TheGrandTour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A discard-to-battlefield replacement does not stop the remaining tour")
    void continuesAfterDiscardToBattlefieldReplacement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ObstinateBaloth());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TheGrandTour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleCardChosen(player2, 0);
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player2, "Obstinate Baloth");
        harness.assertLife(player2, 28);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Aura returning from its owner's library chooses a legal creature to enchant")
    void returningAuraChoosesAttachment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TheGrandTour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, creature.getId());
        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(aura.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getAttachedTo()).isEqualTo(creature.getId());
    }
}
