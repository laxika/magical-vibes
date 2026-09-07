package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RubblebeltBraggart.class)
class RubblebeltBraggartTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets Rubblebelt Braggart become suspected")
    void attackingMaySuspectIt() {
        Permanent braggart = addCreatureReady(player1, new RubblebeltBraggart());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(braggart.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves Rubblebelt Braggart unsuspected")
    void decliningDoesNotSuspectIt() {
        Permanent braggart = addCreatureReady(player1, new RubblebeltBraggart());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(braggart.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("A suspected Rubblebelt Braggart does not trigger when it attacks")
    void suspectedBraggartDoesNotTrigger() {
        Permanent braggart = addCreatureReady(player1, new RubblebeltBraggart());
        braggart.setSuspected(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(braggart.isSuspected()).isTrue();
    }
}
