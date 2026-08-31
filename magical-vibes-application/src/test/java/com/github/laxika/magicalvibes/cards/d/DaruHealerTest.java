package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DaruHealer.class)
class DaruHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage dealt to a targeted player")
    void preventsNextDamageToTargetPlayer() {
        addReadyHealer(player1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new DaruHealer());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new DaruHealer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent healer = findPermanent(player1, "Daru Healer");
        assertThat(healer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        int healerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(healer);
        harness.turnFaceUp(player1, healerIndex);
        harness.passBothPriorities();

        assertThat(healer.isFaceDown()).isFalse();
    }

    private Permanent addReadyHealer(Player player) {
        Permanent healer = new Permanent(new DaruHealer());
        healer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(healer);
        return healer;
    }
}
