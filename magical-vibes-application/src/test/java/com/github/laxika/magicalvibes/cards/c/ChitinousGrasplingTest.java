package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ChitinousGrasplingTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a creature with flying due to reach")
    void canBlockFlyingCreature() {
        Permanent graspling = new Permanent(new ChitinousGraspling());
        graspling.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(graspling);

        Permanent flyer = new Permanent(new AirElemental());
        flyer.setSummoningSick(false);
        flyer.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(flyer);

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
        harness.addToBattlefield(player1, new ChitinousGraspling());

        Permanent graspling = findPermanent(player1, "Chitinous Graspling");

        assertThat(gqs.getEffectivePower(gd, graspling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, graspling)).isEqualTo(5);
    }
}
