package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({ChitinousGraspling.class, AirElemental.class, FieldMarshal.class})
class ChitinousGrasplingTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a creature with flying due to reach")
    void canBlockFlyingCreature() {
        Permanent graspling = harness.addToBattlefieldAndReturn(player2, new ChitinousGraspling());
        graspling.setSummoningSick(false);

        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        flyer.setSummoningSick(false);
        flyer.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Counts as a Soldier for a Soldier lord")
    void countsAsSoldierForLord() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent graspling = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());

        assertThat(gqs.getEffectivePower(gd, graspling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, graspling)).isEqualTo(5);
    }

    @Test
    @DisplayName("Changeling receives a Soldier bonus from an opposing lord")
    void receivesBonusFromOpposingSoldierLord() {
        harness.addToBattlefield(player2, new FieldMarshal());
        Permanent graspling = harness.addToBattlefieldAndReturn(player1, new ChitinousGraspling());

        assertThat(gqs.getEffectivePower(gd, graspling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, graspling)).isEqualTo(5);
    }
}
