package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZealousGuardian.class})
class ZealousGuardianTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    void canFlashInDuringOpponentsEndStep(ManaColor manaColor) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new ZealousGuardian()));
        harness.addMana(player1, manaColor, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Zealous Guardian");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zealous Guardian");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canFlashInInResponseToOpponentsCreatureSpell() {
        ZealousGuardian original = new ZealousGuardian();
        ZealousGuardian response = new ZealousGuardian();
        harness.setHand(player1, List.of(original));
        harness.setHand(player2, List.of(response));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Zealous Guardian");
        harness.assertNotOnBattlefield(player1, "Zealous Guardian");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(original.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zealous Guardian");
        assertThat(gd.stack).isEmpty();
    }
}
