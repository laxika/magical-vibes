package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlimpseTheSunGodTest extends BaseCardTest {

    @Test
    @DisplayName("Taps X target creatures and then scries 1")
    void tapsTargetCreaturesAndScries() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom of the library")
    void scriesToBottom() {
        Card topCard = new Mountain();
        Card nextCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new GlimpseTheSunGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        UUID mountainId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
