package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AggressiveCrag.class)
class AggressiveCragTest extends BaseCardTest {

    @Test
    void tapsItselfAtTheBeginningOfYourCombatStep() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new AggressiveCrag());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        assertThat(crag.isTapped()).isTrue();
    }

    @Test
    void tapsForRedOrWhiteMana() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new AggressiveCrag());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(crag.isTapped()).isTrue();
    }

    @Test
    void tapsForWhiteManaWithoutAddingRedMana() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new AggressiveCrag());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTapDuringOpponentsCombat() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new AggressiveCrag());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.DECLARE_ATTACKERS);

        assertThat(crag.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canProduceManaInResponseToItsCombatTrigger() {
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new AggressiveCrag());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(crag.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
