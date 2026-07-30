package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardiansPledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts white creatures you control by +2/+2")
    void boostsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getPowerModifier()).isEqualTo(2);
        assertThat(vanguard.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost non-white creatures you control")
    void doesNotBoostNonWhiteCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost opponent's white creatures")
    void doesNotBoostOpponentWhiteCreatures() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(vanguard.getPowerModifier()).isEqualTo(0);
        assertThat(vanguard.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new GuardiansPledge()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getPowerModifier()).isEqualTo(0);
        assertThat(vanguard.getToughnessModifier()).isEqualTo(0);
    }
}
