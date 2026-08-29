package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FleetingEffigy.class)
class FleetingEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand at its controller's end step")
    void returnsSelfAtControllerEndStep() {
        addReadyEffigy(player1);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fleeting Effigy");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fleeting Effigy"));
    }

    @Test
    @DisplayName("Does not return itself at an opponent's end step")
    void doesNotReturnAtOpponentEndStep() {
        addReadyEffigy(player1);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Fleeting Effigy");
    }

    @Test
    @DisplayName("Activated ability gives +2/+0 until end of turn")
    void activatedAbilityBoostsPower() {
        Permanent effigy = addReadyEffigy(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, effigy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, effigy)).isEqualTo(2);
    }

    private Permanent addReadyEffigy(Player player) {
        Permanent permanent = new Permanent(new FleetingEffigy());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
