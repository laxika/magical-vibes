package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MorcantsLoyalistTest extends BaseCardTest {

    private void destroyLoyalist() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    void buffsOtherElvesYouControlOnly() {
        harness.addToBattlefield(player1, new MorcantsLoyalist());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent loyalist = findPermanent(player1, "Morcant's Loyalist");
        Permanent ownElf = findPermanent(player1, "Llanowar Elves");
        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingElf = findPermanent(player2, "Llanowar Elves");

        assertThat(gqs.getEffectivePower(gd, loyalist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, loyalist)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingElf)).isEqualTo(1);
    }

    @Test
    void returnsAnotherTargetElfFromGraveyardToHandWhenItDies() {
        Card loyalist = new MorcantsLoyalist();
        Card targetElf = new LlanowarElves();
        Card nonElf = new GrizzlyBears();
        harness.addToBattlefield(player1, loyalist);
        harness.setGraveyard(player1, new ArrayList<>(List.of(targetElf, nonElf)));

        destroyLoyalist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(targetElf.getId());
        assertThat(choice.validCardIds()).doesNotContain(loyalist.getId(), nonElf.getId());

        harness.handleMultipleCardsChosen(player1, List.of(targetElf.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(targetElf.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(loyalist.getId()));
    }

    @Test
    void doesNotTriggerWithoutAnotherElfInGraveyard() {
        Card loyalist = new MorcantsLoyalist();
        harness.addToBattlefield(player1, loyalist);

        destroyLoyalist();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(loyalist.getId()));
    }
}
