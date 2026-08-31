package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VindictiveWarden.class)
class VindictiveWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Firebending adds one red mana until end of combat")
    void firebendingAddsManaUntilEndOfCombat() {
        Permanent warden = addReadyWarden();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(warden.isTapped()).isTrue();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The activated ability deals one damage to each opponent")
    void activatedAbilityDealsDamageToEachOpponent() {
        addReadyWarden();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 1);
    }

    private Permanent addReadyWarden() {
        return addCreatureReady(player1, new VindictiveWarden());
    }
}
