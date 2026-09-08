package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlexVexingPestTest extends BaseCardTest {

    @Test
    void boostsOtherControlledPestsAndNotOpponents() {
        Permanent blex = addCreatureReady(player1, new BlexVexingPest());
        Permanent pest = addCreatureReady(player1, new GrizzlyBears());
        TestCards.mutableCard(pest).setSubtypes(List.of(CardSubtype.PEST));
        Permanent other = addCreatureReady(player1, new HillGiant());
        Permanent opponentPest = addCreatureReady(player2, new GrizzlyBears());
        TestCards.mutableCard(opponentPest).setSubtypes(List.of(CardSubtype.PEST));

        assertThat(gqs.getEffectivePower(gd, blex)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blex)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentPest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentPest)).isEqualTo(2);
    }

    @Test
    void gainsFourLifeWhenItDies() {
        harness.addToBattlefield(player1, new BlexVexingPest());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    void searchForBlexPutsChosenCardsInHandAndLosesThreeLifePerCard() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new HillGiant();
        Card fourth = new Forest();
        Card fifth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        if (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third, fourth, fifth);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }
}
