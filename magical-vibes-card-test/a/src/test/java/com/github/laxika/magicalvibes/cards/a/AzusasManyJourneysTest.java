package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LikenessOfTheSeeker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzusasManyJourneys.class, LikenessOfTheSeeker.class, Forest.class, GrizzlyBears.class})
class AzusasManyJourneysTest extends BaseCardTest {

    @Test
    void chapterIGrantsAnAdditionalLandPlayThisTurn() {
        addSagaWithLore(0);
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToNextChapter();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterIIGainsThreeLife() {
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void chapterIIIReturnsTheSagaTransformedUnderYourControl() {
        addSagaWithLore(2);

        advanceToNextChapter();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LikenessOfTheSeeker)
                .findFirst()
                .orElse(null);
        assertThat(transformed).isNotNull();
        assertThat(transformed.isTransformed()).isTrue();
    }

    @Test
    void transformedFaceUntapsUpToThreeLandsYouControlWhenBlocked() {
        AzusasManyJourneys front = new AzusasManyJourneys();
        Permanent seeker = addCreatureReady(player1, front);
        seeker.setCard(front.getBackFaceCard());
        seeker.setTransformed(true);

        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()),
                harness.addToBattlefieldAndReturn(player1, new Forest()));
        lands.forEach(Permanent::tap);
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        opposingLand.tap();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        seeker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyElementsOf(lands.stream().map(Permanent::getId).toList());
        harness.handleMultiplePermanentsChosen(player1, lands.subList(0, 3).stream().map(Permanent::getId).toList());

        assertThat(lands.subList(0, 3)).allMatch(permanent -> !permanent.isTapped());
        assertThat(lands.get(3).isTapped()).isTrue();
        assertThat(opposingLand.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void transformedCreatureCannotAttackOnTheTurnItReturns() {
        addSagaWithLore(2);

        advanceToNextChapter();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformedFaceCanDeclineToUntapAnyLandsWhenBlocked() {
        Permanent seeker = addCreatureReady(player1, new LikenessOfTheSeeker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        addCreatureReady(player2, new GrizzlyBears());
        seeker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void transformedFaceCanChooseOnlyOneOfTwoLandsWhenBlocked() {
        Permanent seeker = addCreatureReady(player1, new LikenessOfTheSeeker());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        first.tap();
        second.tap();
        addCreatureReady(player2, new GrizzlyBears());
        seeker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void enteringTheBattlefieldTriggersTheFirstChapter() {
        harness.castFromHand(player1, new AzusasManyJourneys(), "{1}{G}");

        resolveAllTriggers();

        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void chapterIIIReturnsAnOpponentsSagaUnderItsControllersControl() {
        AzusasManyJourneys card = new AzusasManyJourneys();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof LikenessOfTheSeeker);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AzusasManyJourneys());
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
