package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        LumberingBattlement card = castBattlement();
        harness.passBothPriorities();
        Permanent battlement = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst().orElseThrow();

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
        LumberingBattlement card = castBattlement();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        Permanent battlement = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst().orElseThrow();
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
        LumberingBattlement battlementCard = castBattlement();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent battlement = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == battlementCard)
                .findFirst().orElseThrow();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, battlement);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, battlement)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, battlement)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does nothing if it leaves before its ETB ability resolves")
    void doesNothingIfSourceLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        LumberingBattlement battlementCard = castBattlement();
        Permanent battlement = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == battlementCard)
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, battlement));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private LumberingBattlement castBattlement() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        LumberingBattlement card = new LumberingBattlement();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return card;
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
