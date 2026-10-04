package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AccessTunnel;
import com.github.laxika.magicalvibes.cards.c.ChargeThrough;
import com.github.laxika.magicalvibes.cards.s.StoneriseSpirit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Humiliate.class, AccessTunnel.class, ChargeThrough.class, StoneriseSpirit.class})
class HumiliateTest extends BaseCardTest {

    @Test
    void discardsChosenNonlandAndPutsCounterOnChosenCreatureYouControl() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new StoneriseSpirit());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new AccessTunnel());
        Card land = new AccessTunnel();
        Card chosenCard = new ChargeThrough();
        Card otherCard = new StoneriseSpirit();
        harness.setHand(player2, List.of(land, chosenCard, otherCard));
        harness.setHand(player1, List.of(new Humiliate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(handChoice.validIndices()).containsExactly(1, 2);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, otherCard);
        PendingInteraction.MultiPermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.validIds()).containsExactlyInAnyOrder(firstCreature.getId(), chosenCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenCreature.getId()));
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(chosenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(noncreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillDiscardsWhenYouControlNoCreature() {
        Card chosenCard = new ChargeThrough();
        harness.setHand(player2, List.of(chosenCard));
        harness.setHand(player1, List.of(new Humiliate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void putsCounterOnCreatureWhenOpponentHandIsEmpty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Humiliate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsCounterOnCreatureWhenOpponentHasOnlyLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        Card land = new AccessTunnel();
        harness.setHand(player2, List.of(land));
        harness.setHand(player1, List.of(new Humiliate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithNoCreaturesAndNoNonlandCardToDiscard() {
        Card land = new AccessTunnel();
        harness.setHand(player2, List.of(land));
        harness.setHand(player1, List.of(new Humiliate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new Humiliate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }
}
