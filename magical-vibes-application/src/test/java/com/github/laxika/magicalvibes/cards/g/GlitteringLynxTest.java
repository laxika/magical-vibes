package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlitteringLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage that would be dealt to it")
    void preventsAllDamageToIt() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, lynx.getId());
        harness.passBothPriorities();

        assertThat(lynx.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lynx);
    }

    @Test
    @DisplayName("Any player may pay to turn off the prevention ability")
    void anyPlayerMayTurnOffPrevention() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, lynx.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, lynx.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lynx);
    }

    @Test
    @DisplayName("The prevention ability returns after end-of-turn cleanup")
    void preventionReturnsAfterEndOfTurn() {
        Permanent lynx = addCreatureReady(player1, new GlitteringLynx());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, lynx.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, lynx.getId());
        harness.passBothPriorities();

        assertThat(lynx.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lynx);
    }
}
