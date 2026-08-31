package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryogenRelic.class, Forest.class, GrizzlyBears.class})
class CryogenRelicTest extends BaseCardTest {

    @Test
    void entersBattlefieldAndDrawsACard() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new CryogenRelic()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cryogen Relic");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void sacrificeAbilityDrawsAndPutsAStunCounterOnTappedCreature() {
        CryogenRelic relic = new CryogenRelic();
        harness.addToBattlefield(player1, relic);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cryogen Relic");
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void sacrificeAbilityCanChooseNoTarget() {
        harness.addToBattlefield(player1, new CryogenRelic());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cryogen Relic");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void abilityCannotTargetAnUntappedCreature() {
        harness.addToBattlefield(player1, new CryogenRelic());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");

        harness.assertOnBattlefield(player1, "Cryogen Relic");
    }
}
