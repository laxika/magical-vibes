package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IroncladRevolutionaryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifice puts two counters on Ironclad Revolutionary and makes each opponent lose 2 life")
    void etbSacrificeArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castIronclad();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        Permanent revolutionary = findPermanent(player1, "Ironclad Revolutionary");
        assertThat(revolutionary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Declining the ETB sacrifice does nothing")
    void declineSacrifice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castIronclad();

        harness.handleMayAbilityChosen(player1, false);

        Permanent revolutionary = findPermanent(player1, "Ironclad Revolutionary");
        assertThat(revolutionary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Only an artifact can be sacrificed for the ETB ability")
    void nonArtifactCannotBeSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castIronclad();

        harness.handleMayAbilityChosen(player1, true);

        Permanent revolutionary = findPermanent(player1, "Ironclad Revolutionary");
        assertThat(revolutionary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void castIronclad() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IroncladRevolutionary()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
