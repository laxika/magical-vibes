package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KolaghanStormsinger.class, ColossodonYearling.class})
class KolaghanStormsingerTest extends BaseCardTest {

    @Test
    void turningFaceUpGivesTargetCreatureHasteUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        Permanent stormsinger = castFaceDown();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stormsinger));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void payingMegamorphCostAddsCounterBeforeHasteTriggerResolves() {
        Permanent stormsinger = castFaceDown();
        assertThat(stormsinger.isFaceDown()).isTrue();
        assertThat(stormsinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stormsinger));

        assertThat(stormsinger.isFaceDown()).isFalse();
        assertThat(stormsinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handlePermanentChosen(player1, stormsinger.getId());
        harness.passBothPriorities();
    }

    @Test
    void turningFaceUpCanGiveAnotherCreatureYouControlHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent stormsinger = castFaceDown();
        assertThat(als.canAttack(gd, target, player1.getId())).isFalse();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stormsinger));
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(als.canAttack(gd, target, player1.getId())).isTrue();
    }

    @Test
    void castingFaceUpDoesNotTriggerHasteGrantOrAddCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.setHand(player1, List.of(new KolaghanStormsinger()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stormsinger = findPermanent(player1, "Kolaghan Stormsinger");
        assertThat(stormsinger.isFaceDown()).isFalse();
        assertThat(stormsinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new KolaghanStormsinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Kolaghan Stormsinger");
    }
}
