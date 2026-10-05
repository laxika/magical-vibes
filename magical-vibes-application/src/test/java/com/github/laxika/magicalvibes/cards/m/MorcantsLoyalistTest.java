package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorcantsLoyalist.class, LlanowarElves.class, GrizzlyBears.class, WrathOfGod.class})
class MorcantsLoyalistTest extends BaseCardTest {

    private void destroyLoyalist() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
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

    @Test
    void multipleLoyalistsBuffEachOtherAndStackOnOtherElves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MorcantsLoyalist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MorcantsLoyalist());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    void canReturnAnElfThatDiesAtTheSameTime() {
        Card loyalist = new MorcantsLoyalist();
        Card elf = new LlanowarElves();
        Card opposingElf = new LlanowarElves();
        harness.addToBattlefield(player1, loyalist);
        harness.addToBattlefield(player1, elf);
        harness.addToBattlefield(player2, opposingElf);

        destroyLoyalist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(elf.getId());
        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(elf.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(elf.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opposingElf.getId()));
    }

    @Test
    void canReturnADifferentLoyalistWithTheSameName() {
        Card loyalist = new MorcantsLoyalist();
        Card anotherLoyalist = new MorcantsLoyalist();
        harness.addToBattlefield(player1, loyalist);
        harness.setGraveyard(player1, new ArrayList<>(List.of(anotherLoyalist)));

        destroyLoyalist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(anotherLoyalist.getId());
        harness.handleMultipleCardsChosen(player1, List.of(anotherLoyalist.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(anotherLoyalist.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(loyalist.getId()))
                .noneMatch(card -> card.getId().equals(anotherLoyalist.getId()));
    }
}
