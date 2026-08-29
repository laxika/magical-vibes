package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EngineeredExplosivesTest extends BaseCardTest {

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new EngineeredExplosives()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent explosives = findPermanent(player1, "Engineered Explosives");
        assertThat(explosives.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void sacrificeDestroysMatchingNonlandPermanents() {
        Permanent explosives = harness.addToBattlefieldAndReturn(player1, new EngineeredExplosives());
        explosives.setCounterCount(CounterType.CHARGE, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Engineered Explosives");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Memnite");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void colorlessSunburstDoesNotDestroyNonzeroManaValuePermanents() {
        harness.setHand(player1, List.of(new EngineeredExplosives()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();

        Permanent explosives = findPermanent(player1, "Engineered Explosives");
        assertThat(explosives.getCounterCount(CounterType.CHARGE)).isZero();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
