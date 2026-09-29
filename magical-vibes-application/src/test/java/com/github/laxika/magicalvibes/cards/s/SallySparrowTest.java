package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SallySparrow.class, GrizzlyBears.class, Shock.class})
class SallySparrowTest extends BaseCardTest {

    @Test
    void controllerCanCastCreatureSpellsAtInstantSpeed() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void onlyControllerGetsCreatureSpellFlash() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void investigatesWhenAnotherControlledCreatureLeavesAndOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new SallySparrow());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        UUID firstBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, firstBearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID secondBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, secondBearId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Clue")).isOne();
    }
}
