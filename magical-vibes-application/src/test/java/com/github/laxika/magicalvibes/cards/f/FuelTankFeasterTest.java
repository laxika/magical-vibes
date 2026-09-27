package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuelTankFeaster.class, CrawWurm.class, GrizzlyBears.class})
class FuelTankFeasterTest extends BaseCardTest {

    @Test
    void reducesTheGreatestManaValueCreatureCardInHand() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new GrizzlyBears(), new CrawWurm()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 1);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Craw Wurm"));
    }

    @Test
    void tapsForOneManaOfAnyColor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new FuelTankFeaster());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void advanceToPrecombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
