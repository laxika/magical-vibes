package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StationMonitor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryogenRelic.class, Forest.class, StationMonitor.class})
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");

        harness.assertOnBattlefield(player1, "Cryogen Relic");
    }

    @Test
    void leavesTriggerResolvesBeforeStunAbility() {
        harness.addToBattlefield(player1, new CryogenRelic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StationMonitor());
        creature.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        harness.assertInGraveyard(player1, "Cryogen Relic");
        harness.assertNotInHand(player1, "Forest");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void untappedTargetReceivesNoCounterButLeavesTriggerStillDraws() {
        harness.addToBattlefield(player1, new CryogenRelic());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StationMonitor());
        creature.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        creature.untap();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Cryogen Relic");
    }

    @Test
    void returningRelicToHandAlsoDraws() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new CryogenRelic());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, relic));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cryogen Relic");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Cryogen Relic");
    }

    @Test
    void abilityCannotTargetTappedNoncreature() {
        harness.addToBattlefield(player1, new CryogenRelic());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");

        harness.assertOnBattlefield(player1, "Cryogen Relic");
    }
}
