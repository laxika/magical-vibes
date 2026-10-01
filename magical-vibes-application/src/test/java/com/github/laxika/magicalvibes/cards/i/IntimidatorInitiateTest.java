package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrimsonWisps;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntimidatorInitiate.class, SafeholdSentry.class, CrimsonWisps.class})
class IntimidatorInitiateTest extends BaseCardTest {

    /** Opponent (player2) casts a red spell so player1's payment mana stays isolated. */
    private void opponentCastsRedSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new IntimidatorInitiate(), "{R}");
    }

    @Test
    @DisplayName("A player casting a red spell prompts the controller's may-pay ability")
    void redSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new IntimidatorInitiate(), "{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} makes the chosen target creature unable to block this turn")
    void payMakesTargetUnableToBlock() {
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        Permanent blocker = addCreatureReady(player2, new SafeholdSentry());

        opponentCastsRedSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

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
        Permanent blocker = addCreatureReady(player2, new SafeholdSentry());

        opponentCastsRedSpell();

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
        harness.castFromHand(player1, new SafeholdSentry(), "{1}{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A red noncreature spell also triggers the may-pay ability")
    void redNonCreatureSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        Permanent target = addCreatureReady(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
