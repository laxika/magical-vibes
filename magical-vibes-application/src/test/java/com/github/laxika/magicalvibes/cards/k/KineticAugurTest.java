package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KineticAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals instant and sorcery cards in its controller's graveyard and toughness stays 4")
    void powerCountsOwnInstantsAndSorceries() {
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new KineticAugur());
        harness.setGraveyard(player1, List.of(new Shock(), new Opt(), new Divination(), new Plains(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, augur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, augur)).isEqualTo(4);
    }

    @Test
    @DisplayName("When it enters, its controller discards up to two cards and draws that many")
    void entersWithRummage() {
        Card discardOne = new GrizzlyBears();
        Card discardTwo = new GrizzlyBears();
        Card kept = new GrizzlyBears();
        Card drawOne = new Forest();
        Card drawTwo = new Mountain();
        harness.setLibrary(player1, List.of(drawOne, drawTwo));
        harness.setHand(player1, List.of(new KineticAugur(), discardOne, discardTwo, kept));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(kept, drawOne, drawTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardOne, discardTwo);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability may discard zero cards")
    void mayDiscardZeroCards() {
        Card kept = new GrizzlyBears();
        harness.setHand(player1, List.of(new KineticAugur(), kept));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
