package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfestTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -2/-2 to all creatures, both players")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new HillGiant()); // 3/3
        harness.addToBattlefield(player2, new HillGiant()); // 3/3

        harness.setHand(player1, List.of(new Infest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent own = giant(player1);
        Permanent opp = giant(player2);
        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(opp.getEffectivePower()).isEqualTo(1);
        assertThat(opp.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures reduced to 0 toughness are destroyed")
    void killsSmallCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new Infest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))).isFalse();
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HillGiant()); // 3/3

        harness.setHand(player1, List.of(new Infest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(giant(player1).getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant(player1).getEffectivePower()).isEqualTo(3);
        assertThat(giant(player1).getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent giant(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
    }
}
