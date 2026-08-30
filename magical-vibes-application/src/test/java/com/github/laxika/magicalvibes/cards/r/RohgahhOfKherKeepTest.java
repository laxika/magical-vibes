package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KherKeep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RohgahhOfKherKeep.class, KherKeep.class, GrizzlyBears.class})
class RohgahhOfKherKeepTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures named Kobolds of Kher Keep that you control")
    void boostsKoboldsYouControl() {
        Permanent kobold = createKoboldToken(player1);
        Permanent opponentKobold = createKoboldToken(player2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addToBattlefield(player1, new RohgahhOfKherKeep());

        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentKobold)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opponentKobold)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying the upkeep cost keeps control and does not tap the permanents")
    void payingUpkeepCostKeepsControl() {
        Permanent kobold = createKoboldToken(player1);
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        harness.addMana(player1, ManaColor.RED, 3);

        beginUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rohgahh, kobold);
        assertThat(rohgahh.isTapped()).isFalse();
        assertThat(kobold.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep cost taps and transfers Rohgahh and all matching creatures")
    void decliningUpkeepCostTransfersAllMatchingCreatures() {
        Permanent kobold = createKoboldToken(player1);
        Permanent opponentKobold = createKoboldToken(player2);
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        beginUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rohgahh.isTapped()).isTrue();
        assertThat(kobold.isTapped()).isTrue();
        assertThat(opponentKobold.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears).doesNotContain(rohgahh, kobold);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rohgahh, kobold).doesNotContain(opponentKobold);
    }

    private void beginUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent createKoboldToken(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new KherKeep());
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.activateAbility(player, 0, 1, null, null);
        harness.passBothPriorities();
        return findPermanent(player, "Kobolds of Kher Keep");
    }
}
