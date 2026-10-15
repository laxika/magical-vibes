package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.ImpassionedOrator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumberingBattlement.class, GrizzlyBears.class, HillGiant.class, Forest.class, ImpassionedOrator.class})
class LumberingBattlementTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers other nontoken creatures you control and boosts for each exiled card")
    void exilesChosenCreaturesAndScales() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card tokenCard = tokenCreature();
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        castBattlement();
        harness.passBothPriorities();
        Permanent battlement = findPermanent(player1, "Lumbering Battlement");

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());
        assertThat(choice.validIds()).doesNotContain(forest.getId(), opponentBears.getId(), token.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), giant.getId()));

        assertThat(gd.getCardsExiledByPermanent(battlement.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, battlement)).isEqualTo(8);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, battlement)).isEqualTo(9);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest, token, battlement);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentBears);
    }

    @Test
    @DisplayName("Exiled creatures return under their owner's control when the source leaves")
    void exiledCreaturesReturnWhenSourceLeaves() {
        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, bearsCard);
        castBattlement();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        Permanent battlement = findPermanent(player1, "Lumbering Battlement");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, battlement));

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing no creatures is legal")
    void mayChooseNoCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBattlement();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent battlement = findPermanent(player1, "Lumbering Battlement");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, battlement);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, battlement)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, battlement)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does nothing if it leaves before its ETB ability resolves")
    void doesNothingIfSourceLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBattlement();
        Permanent battlement = findPermanent(player1, "Lumbering Battlement");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, battlement));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("All exiled creatures return simultaneously and see each other enter")
    void returningCreaturesSeeEachOtherEnter() {
        castBattlement();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ImpassionedOrator());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ImpassionedOrator());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        Permanent battlement = findPermanent(player1, "Lumbering Battlement");
        int lifeBeforeReturn = gd.playerLifeTotals.get(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, battlement));

        assertThat(countPermanents(player1, "Impassioned Orator")).isEqualTo(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBeforeReturn + 2);
    }

    @Test
    @DisplayName("Choosing only some eligible creatures boosts only for the chosen cards")
    void mayChooseSubsetOfCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        castBattlement();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        Permanent battlement = findPermanent(player1, "Lumbering Battlement");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant, battlement).doesNotContain(bears);
        assertThat(gd.getCardsExiledByPermanent(battlement.getId())).containsExactly(bears.getCard());
        assertThat(harness.getGameQueryService().getEffectivePower(gd, battlement)).isEqualTo(6);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, battlement)).isEqualTo(7);
    }

    @Test
    @DisplayName("No choice is needed when there are no other eligible creatures")
    void resolvesWithoutEligibleCreatures() {
        castBattlement();
        harness.passBothPriorities();

        Permanent battlement = findPermanent(player1, "Lumbering Battlement");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(battlement.getId())).isEmpty();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, battlement)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, battlement)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exiled creatures return as new untapped permanents without their old counters")
    void returningCreatureLosesCountersAndTappedState() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.tap();
        castBattlement();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        Permanent battlement = findPermanent(player1, "Lumbering Battlement");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, battlement));

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertInHand(player1, "Lumbering Battlement");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castBattlement() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        LumberingBattlement card = new LumberingBattlement();
        harness.castFromHand(player1, card, "{4}{W}");
        harness.passBothPriorities();
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Creature Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
