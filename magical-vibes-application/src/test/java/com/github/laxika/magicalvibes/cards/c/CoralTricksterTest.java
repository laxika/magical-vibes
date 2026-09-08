package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoralTrickster.class, GrizzlyBears.class})
class CoralTricksterTest extends BaseCardTest {

    @Test
    void turningFaceUpTapsAnUntappedTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        turnFaceUpCoralTrickster(target);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void turningFaceUpUntapsATappedTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        turnFaceUpCoralTrickster(target);

        assertThat(target.isTapped()).isFalse();
    }

    private void turnFaceUpCoralTrickster(Permanent target) {
        harness.setHand(player1, List.of(new CoralTrickster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent coralTrickster = findPermanent(player1, "Coral Trickster");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(coralTrickster));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
