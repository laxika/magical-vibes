package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoulNetTest extends BaseCardTest {

    @Test
    @DisplayName("When a creature dies, controller may pay {1} to gain 1 life")
    void creatureDiesPayGainLife() {
        harness.addToBattlefield(player1, new SoulNet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        // Kill the opponent's creature with Shock.
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // to pay {1}

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger placed
        harness.passBothPriorities(); // Resolve Soul Net trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Declining the may-pay gains no life")
    void declineNoLife() {
        harness.addToBattlefield(player1, new SoulNet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Accepting without enough mana gains no life")
    void acceptWithoutManaNoLife() {
        harness.addToBattlefield(player1, new SoulNet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        // No spare mana to pay {1}.

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Triggers when the controller's own creature dies")
    void ownCreatureDiesTriggers() {
        harness.addToBattlefield(player1, new SoulNet());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // to pay {1}

        // Opponent kills the controller's creature.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }
}
