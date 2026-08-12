package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TerraStomperTest extends BaseCardTest {

    @Test
    @DisplayName("Terra Stomper cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        TerraStomper stomper = new TerraStomper();
        harness.setHand(player1, List.of(stomper));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, stomper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Terra Stomper");
        harness.assertNotInGraveyard(player1, "Terra Stomper");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Terra Stomper cannot be countered by a counter-unless-pays ability")
    void cannotBeCounteredByCounterUnlessPays() {
        harness.addToBattlefield(player2, new SpiketailHatchling());

        TerraStomper stomper = new TerraStomper();
        harness.setHand(player1, List.of(stomper));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, stomper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Terra Stomper");
    }

    @Test
    @DisplayName("Terra Stomper deals excess combat damage to the defending player")
    void trampleDealsExcessDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent stomper = addStomperReady(player1);
        stomper.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addStomperReady(Player player) {
        TerraStomper card = new TerraStomper();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
