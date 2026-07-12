package com.github.laxika.magicalvibes.cards.n;

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

class NurturerInitiateTest extends BaseCardTest {

    /** Opponent (player2) casts a green spell so player1's payment mana stays isolated. */
    private void opponentCastsGreenSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
    }

    @Test
    @DisplayName("A player casting a green spell prompts the controller's may-pay ability")
    void greenSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} gives the chosen target creature +1/+1 until end of turn")
    void payBoostsTargetCreature() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        Permanent target = addReadyCreature(player2);

        opponentCastsGreenSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        Permanent target = addReadyCreature(player2);

        opponentCastsGreenSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining leaves the target creature unboosted")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        Permanent target = addReadyCreature(player2);

        opponentCastsGreenSpell();

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a non-green spell does not trigger the ability")
    void nonGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 6);

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
