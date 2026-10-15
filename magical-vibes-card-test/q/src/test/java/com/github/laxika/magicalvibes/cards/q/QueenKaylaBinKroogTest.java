package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.m.MineWorker;
import com.github.laxika.magicalvibes.cards.s.StinkweedImp;
import com.github.laxika.magicalvibes.cards.t.TowerWorker;
import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QueenKaylaBinKroog.class, GiantGrowth.class, GrizzlyBears.class,
        LlanowarElves.class, Manalith.class, Forest.class, YotianFrontliner.class,
        MineWorker.class, TowerWorker.class, AlmsCollector.class, StinkweedImp.class})
class QueenKaylaBinKroogTest extends BaseCardTest {

    @Test
    void discardsDrawsAndReturnsOneEligibleCardAtEachManaValue() {
        Card manaValueOneCreature = new LlanowarElves();
        Card manaValueTwoCreature = new GrizzlyBears();
        Card manaValueThreeArtifact = new Manalith();
        Card ineligibleCard = new GiantGrowth();
        QueenKaylaBinKroog queen = new QueenKaylaBinKroog();

        addCreatureReady(player1, queen);
        harness.setHand(player1, List.of(manaValueOneCreature, manaValueTwoCreature,
                manaValueThreeArtifact, ineligibleCard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        choose(player1, manaValueOneCreature);
        choose(player1, manaValueTwoCreature);
        choose(player1, manaValueThreeArtifact);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(manaValueOneCreature.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(manaValueTwoCreature.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(manaValueThreeArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ineligibleCard.getId()))
                .noneMatch(card -> card.getId().equals(manaValueOneCreature.getId()))
                .noneMatch(card -> card.getId().equals(manaValueTwoCreature.getId()))
                .noneMatch(card -> card.getId().equals(manaValueThreeArtifact.getId()));
    }

    @Test
    void waitsForAllChoicesBeforeReturningCards() {
        Card frontliner = new YotianFrontliner();
        Card mine = new MineWorker();
        Card tower = new TowerWorker();
        addCreatureReady(player1, new QueenKaylaBinKroog());
        harness.setHand(player1, List.of(frontliner, mine, tower));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        choose(player1, frontliner);
        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        choose(player1, mine);
        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        harness.assertNotOnBattlefield(player1, "Mine Worker");
        choose(player1, tower);

        harness.assertOnBattlefield(player1, "Yotian Frontliner");
        harness.assertOnBattlefield(player1, "Mine Worker");
        harness.assertOnBattlefield(player1, "Tower Worker");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclineOneManaValueAndStillChooseAnother() {
        Card frontliner = new YotianFrontliner();
        Card mine = new MineWorker();
        addCreatureReady(player1, new QueenKaylaBinKroog());
        harness.setHand(player1, List.of(frontliner, mine));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);
        choose(player1, mine);

        harness.assertInGraveyard(player1, "Yotian Frontliner");
        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        harness.assertOnBattlefield(player1, "Mine Worker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void onlyOffersCardsDiscardedThisWayAndReturnsOnePerManaValue() {
        Card oldCard = new YotianFrontliner();
        Card first = new YotianFrontliner();
        Card second = new YotianFrontliner();
        addCreatureReady(player1, new QueenKaylaBinKroog());
        harness.setGraveyard(player1, List.of(oldCard));
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).cardPool())
                .containsExactly(first, second);
        choose(player1, second);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldCard, first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(second.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyHandDoesNotDrawOrReturnAnOldGraveyardCard() {
        addCreatureReady(player1, new QueenKaylaBinKroog());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new YotianFrontliner()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Yotian Frontliner");
        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateOutsideAMainPhase() {
        addCreatureReady(player1, new QueenKaylaBinKroog());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void almsCollectorReplacesTheWholeDrawInstruction() {
        addCreatureReady(player1, new QueenKaylaBinKroog());
        harness.addToBattlefield(player2, new AlmsCollector());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void offersDredgeBeforeChoosingCardsToReturn() {
        Card imp = new StinkweedImp();
        addCreatureReady(player1, new QueenKaylaBinKroog());
        harness.setHand(player1, List.of(imp));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.destination()).isEqualTo(GraveyardChoiceDestination.DREDGE);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(imp);
        harness.assertNotOnBattlefield(player1, "Stinkweed Imp");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void choose(com.github.laxika.magicalvibes.model.Player player, Card card) {
        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.GraveyardChoice.class);
        int index = java.util.stream.IntStream.range(0, choice.cardPool().size())
                .filter(i -> choice.cardPool().get(i).getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player, index);
    }
}
