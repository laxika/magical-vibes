package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarKyoshiEarthbender.class, Forest.class})
class AvatarKyoshiEarthbenderTest extends BaseCardTest {

    @Test
    void earthbendsAndUntapsTargetLandAtBeginningOfOwnCombat() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void hasHexproofOnlyDuringItsControllerTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new AvatarKyoshiEarthbender());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, avatar, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, avatar, Keyword.HEXPROOF)).isFalse();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
