package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CaptainsCall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HealerOfThePride.class, GrizzlyBears.class, CaptainsCall.class})
class HealerOfThePrideTest extends BaseCardTest {

    @Test
    @DisplayName("Gain 2 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new HealerOfThePride());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Grizzly Bears -> enters, life trigger on stack
        harness.passBothPriorities(); // resolve life trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("No life gained when an opponent's creature enters")
    void noLifeOnOpponentCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new HealerOfThePride());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve Grizzly Bears

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No life gained when Healer of the Pride itself enters")
    void noLifeOnSelfEnter() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HealerOfThePride()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Healer of the Pride

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each creature token entering creates a separate life gain trigger")
    void gainsLifeForEachTokenEntering() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new HealerOfThePride());
        harness.setHand(player1, List.of(new CaptainsCall()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(3);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An existing Healer triggers for a second Healer, but the entering Healer does not trigger for itself")
    void existingHealerTriggersForAnotherHealer() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new HealerOfThePride());
        harness.setHand(player1, List.of(new HealerOfThePride()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }
}
