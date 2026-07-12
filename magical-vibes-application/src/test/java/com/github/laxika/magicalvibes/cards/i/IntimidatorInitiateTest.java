package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntimidatorInitiateTest extends BaseCardTest {

    /** Opponent (player2) casts a red spell so player1's payment mana stays isolated. */
    private void opponentCastsRedSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 6);
    }

    @Test
    @DisplayName("A player casting a red spell prompts the controller's may-pay ability")
    void redSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} makes the chosen target creature unable to block this turn")
    void payMakesTargetUnableToBlock() {
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        Permanent blocker = addReadyCreature(player2);

        opponentCastsRedSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining leaves the target creature able to block")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        Permanent blocker = addReadyCreature(player2);

        opponentCastsRedSpell();

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(blocker.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-red spell does not trigger the ability")
    void nonRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
