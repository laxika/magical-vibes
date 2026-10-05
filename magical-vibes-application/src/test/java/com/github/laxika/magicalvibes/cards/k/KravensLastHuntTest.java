package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KravensLastHunt.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class KravensLastHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills five, then deals damage based on the greatest creature power")
    void chapterIMillsThenDealsGreatestPowerDamage() {
        HillGiant graveyardCreature = new HillGiant();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
    }

    @Test
    @DisplayName("Chapter II boosts a target creature you control")
    void chapterIIBoostsCreatureYouControl() {
        addSaga(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(ownCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opposingCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III returns a target creature card from the graveyard")
    void chapterIIIReturnsCreatureCardToHand() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureCard));
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creatureCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creatureCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creatureCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creatureCard);
    }

    @Test
    @DisplayName("Chapter I includes newly milled creatures and mills a short library")
    void chapterIUsesNewlyMilledCreature() {
        HillGiant milledCreature = new HillGiant();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), milledCreature));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Chapter I deals no damage without creature cards in your graveyard")
    void chapterIDealsZeroWithoutCreatureCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Chapter I determines greatest power when its reflexive ability resolves")
    void chapterIChecksGraveyardAtDamageResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III targets only your creature cards and sacrifices the Saga after resolution")
    void chapterIIIFiltersGraveyardAndSacrificesSaga() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        HillGiant opponentCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setGraveyard(player2, List.of(opponentCreature));
        Permanent saga = addSaga(2);

        triggerChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, saga.getCard()).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    @DisplayName("Chapter I still mills when there are no creatures to target")
    void chapterIMillsWithoutTargets() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter II's boost expires at the end of the turn")
    void chapterIIBoostExpires() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(1);

        triggerChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new KravensLastHunt());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
