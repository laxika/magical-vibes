package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterspoutWarden.class, ThreeTreeMascot.class, Island.class})
class WaterspoutWardenTest extends BaseCardTest {

    @Test
    void gainsFlyingWhenAnotherCreatureEnteredUnderYourControlThisTurn() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());
        recordEntry(player1.getId(), new ThreeTreeMascot());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotGainFlyingWithoutAnotherCreatureEntry() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotCountOpponentOrItsOwnEntry() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());
        recordEntry(player2.getId(), new ThreeTreeMascot());
        recordEntry(player1.getId(), warden.getCard());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
    }

    @Test
    void gainsFlyingAfterAnotherCreatureActuallyEnters() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());
        harness.castFromHand(player1, new ThreeTreeMascot(), "{2}");
        resolveAllTriggers();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();
    }

    @Test
    void anotherWardenCountsAsAnotherCreature() {
        Permanent warden = addCreatureReady(player1, new WaterspoutWarden());
        harness.castFromHand(player1, new WaterspoutWarden(), "{2}{U}");
        resolveAllTriggers();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotTriggerWhenOnlyANoncreatureEntered() {
        addCreatureReady(player1, new WaterspoutWarden());
        recordEntry(player1.getId(), new Island());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerBeforeAnotherCreatureEnters() {
        addCreatureReady(player1, new WaterspoutWarden());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
    }

    private void recordEntry(UUID controllerId, Card card) {
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                .add(card);
    }
}
