package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.d.DelversTorch;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeenEaredSentry.class, BeaconOfImmortality.class, DelversTorch.class, GrizzlyBears.class})
class KeenEaredSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives its controller hexproof")
    void givesControllerHexproof() {
        harness.addToBattlefield(player1, new KeenEaredSentry());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Limits each opponent to one dungeon venture per turn")
    void limitsOpponentDungeonVenturesPerTurn() {
        harness.addToBattlefield(player1, new KeenEaredSentry());
        addEquippedCreature(player2);
        addEquippedCreature(player2);

        declareAttackers(player2, List.of(0, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Does not limit its controller's dungeon ventures")
    void doesNotLimitControllerDungeonVentures() {
        harness.addToBattlefield(player1, new KeenEaredSentry());
        addEquippedCreature(player1);
        addEquippedCreature(player1);

        declareAttackers(player1, List.of(1, 3));
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));
    }

    private void addEquippedCreature(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        Permanent torch = new Permanent(new DelversTorch());
        torch.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player.getId()).add(torch);
    }
}
