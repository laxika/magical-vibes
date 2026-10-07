package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFlux.class, GrizzlyBears.class, Shock.class, Mountain.class, VorinclexMonstrousRaider.class})
class TheFluxTest extends BaseCardTest {

    @Test
    void chapterIDealsFourDamageToAcreatureAnOpponentControls() {
        GrizzlyBears opponentCard = new GrizzlyBears();
        opponentCard.setToughness(5);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, opponentCard);
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void chapterIIExilesTheTopCardAndAllowsPlayingIt() {
        assertChapterExilesTopCardAndAllowsPlaying(1);
    }

    @Test
    void chapterVExilesTheTopCardAndAllowsPlayingIt() {
        assertChapterExilesTopCardAndAllowsPlaying(4);
    }

    @Test
    void chapterVIAddsSixRedManaAndSacrificesTheSaga() {
        Permanent saga = addSagaWithLore(5);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterIIIExilesTheTopCardAndAllowsPlayingIt() {
        assertChapterExilesTopCardAndAllowsPlaying(2);
    }

    @Test
    void chapterIVExilesTheTopCardAndAllowsPlayingIt() {
        assertChapterExilesTopCardAndAllowsPlaying(3);
    }

    @Test
    void castingTriggersChapterIAndExcludesControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new TheFlux(), "{2}{R}{R}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature.getCard());
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "The Flux");
    }

    @Test
    void chapterIWithoutLegalTargetsStillAllowsChapterII() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent saga = addSagaWithLore(0);
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void emptyLibraryDoesNotPreventChapterIIFromResolving() {
        harness.setLibrary(player1, List.of());
        Permanent saga = addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exiledSpellRequiresPaymentAndCanBeCastThisTurn() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void exiledLandUsesNormalTimingAndLandPlayLimit() {
        Mountain topCard = new Mountain();
        Mountain handLand = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(handLand));
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(topCard.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unplayedCardStaysExiledButPermissionExpiresAfterThisTurn() {
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void finalChapterUsesTheStackAndSagaRemainsUntilItResolves() {
        Permanent saga = addSagaWithLore(5);

        advanceToNextChapter();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void opposingVorinclexPreventsTurnBasedLoreCounterAndChapterTrigger() {
        harness.addToBattlefield(player2, new VorinclexMonstrousRaider());
        Permanent saga = addSagaWithLore(1);
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        advanceToNextChapter();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void controlledVorinclexDoublesTurnBasedLoreCountersAndTriggersBothCrossedChapters() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        Permanent saga = addSagaWithLore(1);
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setLibrary(player1, List.of(first, second));

        advanceToNextChapter();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void enteringWithTwoLoreCountersTriggersBothChapterIAndChapterII() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, new TheFlux(), "{2}{R}{R}");
        harness.passBothPriorities();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof TheFlux).findFirst().orElseThrow();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    private void assertChapterExilesTopCardAndAllowsPlaying(int loreCounters) {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        addSagaWithLore(loreCounters);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlux());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
