package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StorrevDevkarinLich.class, GrizzlyBears.class, ChandraNalaar.class,
        NarsetParterOfVeils.class, HillGiant.class})
class StorrevDevkarinLichTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureCardToHandAfterDealingCombatDamageToPlayer() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        storrev.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void returnsTargetPlaneswalkerCardAfterDealingCombatDamageToPlaneswalker() {
        Card target = new ChandraNalaar();
        harness.setGraveyard(player1, List.of(target));
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        Permanent narset = addCreatureReady(player2, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 6);
        storrev.setAttacking(true);

        declareAttackersAtPlaneswalker(narset);
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void cannotTargetACardPutIntoAGraveyardDuringTheSameCombat() {
        Permanent storrev = addCreatureReady(player1, new StorrevDevkarinLich());
        Permanent dyingAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        storrev.setAttacking(true);
        dyingAttacker.setAttacking(true);

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dyingAttacker.getCard().getId()));
    }

    private void declareAttackersAtPlaneswalker(Permanent planeswalker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }
}
