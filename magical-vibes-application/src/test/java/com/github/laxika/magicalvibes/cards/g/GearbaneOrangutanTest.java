package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GearbaneOrangutan.class, Ornithopter.class})
class GearbaneOrangutanTest extends BaseCardTest {

    @Test
    void destroyModeDestroysAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castOrangutan();
        harness.handleListChoice(player1, "Destroy up to one target artifact.");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void destroyModeCanDeclineItsOptionalTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castOrangutan();
        harness.handleListChoice(player1, "Destroy up to one target artifact.");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    void sacrificeModePutsCountersOnTheOrangutanAfterSacrifice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castOrangutan();
        harness.handleListChoice(player1,
                "Sacrifice an artifact. If you do, put two +1/+1 counters on this creature.");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        Permanent orangutan = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GearbaneOrangutan)
                .findFirst()
                .orElseThrow();
        assertThat(orangutan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    private void castOrangutan() {
        harness.setHand(player1, List.of(new GearbaneOrangutan()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
