package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PropheticBolt.class, GrizzlyBears.class, Shock.class, Forest.class})
class PropheticBoltTest extends BaseCardTest {

    private Card[] setTopFour() {
        Card top1 = new GrizzlyBears();
        Card top2 = new Shock();
        Card top3 = new GrizzlyBears();
        Card top4 = new Shock();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(top1, top2, top3, top4));
        return new Card[]{top1, top2, top3, top4};
    }

    private void addPropheticBoltMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    void dealsFourDamageToPlayerThenPutsChosenCardIntoHand() {
        harness.setHand(player1, List.of(new PropheticBolt()));
        Card[] top = setTopFour();
        addPropheticBoltMana();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);

        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(top[0]);

        PendingInteraction.LibraryReorder reorderInteraction =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorderInteraction.cards()).containsExactlyInAnyOrder(top[1], top[2], top[3]);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(
                        reorderInteraction.cards().indexOf(top[1]),
                        reorderInteraction.cards().indexOf(top[2]),
                        reorderInteraction.cards().indexOf(top[3]))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top[1], top[2], top[3]);
        harness.assertInGraveyard(player1, "Prophetic Bolt");
    }

    @Test
    void dealsFourDamageToTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PropheticBolt()));
        Card[] top = setTopFour();
        addPropheticBoltMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(top[0].getId()));
        PendingInteraction.LibraryReorder reorderInteraction =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(
                        reorderInteraction.cards().indexOf(top[1]),
                        reorderInteraction.cards().indexOf(top[2]),
                        reorderInteraction.cards().indexOf(top[3]))));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PropheticBolt()));
        addPropheticBoltMana();
        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }
}
