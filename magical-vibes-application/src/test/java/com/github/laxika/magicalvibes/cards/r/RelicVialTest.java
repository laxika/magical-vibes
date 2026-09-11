package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ClericOfLifesBond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelicVial.class, ClericOfLifesBond.class, GrizzlyBears.class})
class RelicVialTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature draws a card")
    void sacrificingCreatureDrawsCard() {
        harness.addToBattlefield(player1, new RelicVial());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With a Cleric, a dying creature drains each opponent")
    void clericEnablesDeathTrigger() {
        harness.addToBattlefield(player1, new RelicVial());
        harness.addToBattlefield(player1, new ClericOfLifesBond());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setMarkedDamage(3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Without a Cleric, a dying creature does not drain")
    void clericDeathTriggerIsInactiveWithoutCleric() {
        harness.addToBattlefield(player1, new RelicVial());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setMarkedDamage(3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
