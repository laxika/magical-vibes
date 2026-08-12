package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherVialTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a charge counter on Aether Vial")
    void upkeepAcceptedAddsChargeCounter() {
        Permanent vial = addVial(player1);

        advanceToUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a creature with matching mana value onto the battlefield")
    void putsCreatureWithMatchingManaValue() {
        Permanent vial = addVial(player1);
        vial.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(vial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not offer a creature with a different mana value")
    void requiresMatchingManaValue() {
        Permanent vial = addVial(player1);
        vial.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Llanowar Elves");
    }

    private Permanent addVial(Player owner) {
        Permanent vial = new Permanent(new AetherVial());
        vial.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(vial);
        return vial;
    }

    private void advanceToUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
