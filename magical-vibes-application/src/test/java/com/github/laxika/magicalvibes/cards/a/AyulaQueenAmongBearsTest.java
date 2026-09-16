package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AyulaQueenAmongBears.class, GrizzlyBears.class, HillGiant.class})
class AyulaQueenAmongBearsTest extends BaseCardTest {

    @Test
    @DisplayName("A Bear entering lets Ayula put two counters on a target Bear")
    void putsCountersOnTargetBear() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBear();

        harness.handleListChoice(player1, "Put two +1/+1 counters on target Bear.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Bear entering lets Ayula make a Bear you control fight an opposing creature")
    void fightsTargetCreature() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBear();

        harness.handleListChoice(player1,
                "Target Bear you control fights target creature you don't control.");
        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.handlePermanentChosen(player1, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear);
    }

    @Test
    @DisplayName("A non-Bear entering does not trigger Ayula")
    void nonBearDoesNotTrigger() {
        harness.addToBattlefield(player1, new AyulaQueenAmongBears());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBear() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
