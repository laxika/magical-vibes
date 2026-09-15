package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RorixBladewing.class, ElvishWarrior.class})
class RorixBladewingTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Rorix Bladewing attack the turn it enters the battlefield")
    void hasteLetsItAttackImmediately() {
        harness.setHand(player1, List.of(new RorixBladewing()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Rorix Bladewing")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new RorixBladewing());
        addCreatureReady(player2, new ElvishWarrior());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }
}
