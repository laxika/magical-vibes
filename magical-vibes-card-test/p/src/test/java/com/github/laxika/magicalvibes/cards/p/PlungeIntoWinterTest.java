package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UnassumingSage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlungeIntoWinter.class, Forest.class, UnassumingSage.class})
class PlungeIntoWinterTest extends BaseCardTest {

    @Test
    void tapsTargetCreatureThenScriesAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castPlunge(creature.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayBeCastWithoutChoosingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castPlunge(null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void bottomingScryCardDrawsTheNextCard() {
        Card topCard = new Forest();
        Card nextCard = new UnassumingSage();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        castPlunge(null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOwnAlreadyTappedCreatureAndStillScryAndDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        creature.tap();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castPlunge(creature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void doesNotScryOrDrawWhenChosenTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new PlungeIntoWinter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(PlungeIntoWinter.class::isInstance);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PlungeIntoWinter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPlunge(UUID targetId) {
        harness.setHand(player1, List.of(new PlungeIntoWinter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (targetId == null) {
            harness.castAndResolveInstant(player1, 0);
        } else {
            harness.castAndResolveInstant(player1, 0, targetId);
        }
    }
}
