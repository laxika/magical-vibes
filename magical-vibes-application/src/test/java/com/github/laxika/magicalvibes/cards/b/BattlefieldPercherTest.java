package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.s.StrongholdZeppelin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldPercher.class, StrongholdZeppelin.class, Mossdog.class})
class BattlefieldPercherTest extends BaseCardTest {

    @Test
    void canBlockCreatureWithFlying() {
        Permanent percher = addCreatureReady(player2, new BattlefieldPercher());
        addCreatureReady(player1, new StrongholdZeppelin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(percher.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockCreatureWithoutFlying() {
        addCreatureReady(player2, new BattlefieldPercher());
        addCreatureReady(player1, new Mossdog());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent percher = addCreatureReady(player1, new BattlefieldPercher());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(percher.getEffectivePower()).isEqualTo(3);
        assertThat(percher.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(percher.getEffectivePower()).isEqualTo(2);
        assertThat(percher.getEffectiveToughness()).isEqualTo(2);
    }
}
