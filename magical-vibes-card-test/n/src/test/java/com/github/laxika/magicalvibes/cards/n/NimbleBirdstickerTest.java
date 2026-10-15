package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimbleBirdsticker.class, AirElemental.class})
class NimbleBirdstickerTest extends BaseCardTest {

    @Test
    @DisplayName("Nimble Birdsticker can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent birdsticker = harness.addToBattlefieldAndReturn(player2, new NimbleBirdsticker());
        birdsticker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(birdsticker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach does not evade ground blockers and summoning sickness does not prevent blocking")
    void summoningSickBirdstickerCanBlockBirdsticker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NimbleBirdsticker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NimbleBirdsticker());
        blocker.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
