package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Carbonize;
import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Recuperate.class, ElvishAberration.class, Carbonize.class})
class RecuperateTest extends BaseCardTest {

    @Test
    @DisplayName("Life-gain mode gives the controller 6 life")
    void lifeGainMode() {
        harness.setLife(player1, 10);
        cast(0, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Prevention mode prevents the next 6 damage to the target creature")
    void preventionMode() {
        Permanent protectedCreature = addCreatureReady(player1, new ElvishAberration());
        Permanent otherCreature = addCreatureReady(player2, new ElvishAberration());
        cast(1, protectedCreature.getId());

        harness.setHand(player1, List.of(
                new Carbonize(), new Carbonize(), new Carbonize(), new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castAndResolveInstant(player1, 0, otherCreature.getId());
        harness.castAndResolveInstant(player1, 0, protectedCreature.getId());
        harness.castAndResolveInstant(player1, 0, protectedCreature.getId());
        harness.castAndResolveInstant(player1, 0, protectedCreature.getId());

        assertThat(otherCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevention mode expires at the end of the turn")
    void preventionModeExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new ElvishAberration());
        cast(1, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevention mode rejects a player target")
    void preventionModeRequiresCreatureTarget() {
        harness.setHand(player1, List.of(new Recuperate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new Recuperate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
