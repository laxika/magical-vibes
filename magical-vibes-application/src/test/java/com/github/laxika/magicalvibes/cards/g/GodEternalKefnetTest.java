package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodEternalKefnet.class, Divination.class, Forest.class, Mountain.class, Plains.class,
        SwordsToPlowshares.class, WrathOfGod.class})
class GodEternalKefnetTest extends BaseCardTest {

    @Test
    @DisplayName("The first drawn instant or sorcery is copied and may be cast for two less")
    void copiesFirstDrawnInstantOrSorcery() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Divination(), new Forest(), new Mountain(), new Plains()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Declining to reveal the first drawn instant or sorcery creates no copy")
    void mayDeclineReveal() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Divination(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A drawn nonland card that is not an instant or sorcery is only revealed")
    void doesNotCopyOtherCardTypes() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The death trigger may put Kefnet third from the top")
    void deathTriggerPutsKefnetThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new GodEternalKefnet());
        Card kefnet = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), kefnet.getId(), third.getId());
    }

    @Test
    @DisplayName("The exile trigger may put Kefnet third from the top")
    void exileTriggerPutsKefnetThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        var kefnetPermanent = harness.addToBattlefieldAndReturn(player1, new GodEternalKefnet());
        Card kefnet = kefnetPermanent.getCard();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, kefnetPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), kefnet.getId(), third.getId());
    }
}
