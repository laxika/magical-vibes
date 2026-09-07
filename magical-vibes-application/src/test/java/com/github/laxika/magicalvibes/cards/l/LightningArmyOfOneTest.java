package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningArmyOfOne.class, GrizzlyBears.class, Shock.class})
class LightningArmyOfOneTest extends BaseCardTest {

    @Test
    @DisplayName("Stagger doubles damage to the damaged player and their permanents")
    void staggerDoublesDamageToDamagedPlayerAndTheirPermanents() {
        harness.setLife(player2, 20);
        Permanent lightning = addCreatureReady(player1, new LightningArmyOfOne());
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lightning)));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();

        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Stagger does not double damage to another player")
    void staggerDoesNotDoubleDamageToAnotherPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lightning = addCreatureReady(player1, new LightningArmyOfOne());
        harness.setHand(player1, List.of(new Shock()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lightning)));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Stagger expires at its controller's next turn")
    void staggerExpiresAtControllersNextTurn() {
        harness.setLife(player2, 20);
        Permanent lightning = addCreatureReady(player1, new LightningArmyOfOne());
        harness.setHand(player1, List.of(new Shock(), new Shock()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lightning)));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);

        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }
}
