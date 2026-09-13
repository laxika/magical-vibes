package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.o.OKagachiMadeManifest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheKamiWar.class, OKagachiMadeManifest.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Shock.class})
class TheKamiWarTest extends BaseCardTest {

    @Test
    void chapterIExilesOnlyAnOpponentsNonlandPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpposingPermanent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(
                opposingPermanent.getId(), secondOpposingPermanent.getId());

        harness.handlePermanentChosen(player1, opposingPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(ownPermanent.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(opposingPermanent.getId()).contains(secondOpposingPermanent.getId(), opposingLand.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingPermanent.getCard());
    }

    @Test
    void chapterIIReturnsAnotherNonlandPermanentThenEachOpponentDiscards() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new Forest());
        Card discarded = new Shock();
        harness.setHand(player2, List.of(discarded));
        addSagaWithLore(1);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(ownPermanent.getId(), opposingPermanent.getId());

        harness.handlePermanentChosen(player1, opposingPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(opposingPermanent.getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(opposingPermanent.getCard().getId()).doesNotContain(discarded.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void chapterIIDiscardsEvenWhenNoOtherNonlandPermanentCanBeChosen() {
        Card discarded = new Shock();
        harness.setHand(player2, List.of(discarded));
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    void chapterIIIReturnsTheSagaTransformed() {
        addSagaWithLore(2);

        advanceToNextChapter();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof OKagachiMadeManifest)
                .findFirst()
                .orElse(null);
        assertThat(transformed).isNotNull();
        assertThat(transformed.isTransformed()).isTrue();
    }

    @Test
    void transformedFaceLetsDefendingPlayerChooseANonlandCardAndBoostsItsPower() {
        TheKamiWar front = new TheKamiWar();
        Permanent kami = new Permanent(front);
        kami.setCard(front.getBackFaceCard());
        kami.setTransformed(true);
        kami.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kami);

        Card chosen = new HillGiant();
        Card otherNonland = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(chosen, otherNonland, land));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(chosen.getId(), otherNonland.getId());

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(chosen);
        assertThat(kami.getPowerModifier()).isEqualTo(chosen.getManaValue());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheKamiWar());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
