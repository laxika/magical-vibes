package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fastbond.class, Forest.class, Disenchant.class})
class FastbondTest extends BaseCardTest {

    @Test
    @DisplayName("Allows multiple land plays and damages its controller after the first")
    void allowsMultipleLandPlaysAndDamagesControllerAfterFirst() {
        harness.castFromHand(player1, new Fastbond(), "{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        harness.assertLife(player1, 20);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
        harness.assertLife(player1, 19);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(3);
        harness.assertLife(player1, 18);
    }

    @Test
    void opponentLandPlayDoesNotTriggerFastbond() {
        harness.addToBattlefield(player1, new Fastbond());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void landEnteringWithoutBeingPlayedDoesNotTriggerFastbond() {
        harness.addToBattlefield(player1, new Fastbond());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.assertLife(player1, 20);
        assertThat(gd.landsPlayedThisTurn).doesNotContainKey(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterFastbondLeavesBattlefield() {
        var fastbondPermanent = harness.addToBattlefieldAndReturn(player1, new Fastbond());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fastbondPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(fastbondPermanent.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(fastbondPermanent.getCard());
    }
}
