package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.IntimidatorInitiate;
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

@CardUsed({NurturerInitiate.class, IntimidatorInitiate.class})
class NurturerInitiateTest extends BaseCardTest {

    /** Opponent casts a green spell so the controller's payment mana stays isolated. */
    private void opponentCastsGreenSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NurturerInitiate()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
    }

    @Test
    @DisplayName("The target is chosen before the controller decides whether to pay at resolution")
    void greenSpellTriggersMayPay() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NurturerInitiate());
        harness.setHand(player1, List.of(new NurturerInitiate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, source.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(source.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying {1} gives the chosen opposing creature +1/+1")
    void payBoostsTargetCreature() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntimidatorInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        opponentCastsGreenSpell();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntimidatorInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        opponentCastsGreenSpell();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining at resolution leaves the targeted creature unboosted and spends no mana")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntimidatorInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        opponentCastsGreenSpell();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-green spell does not trigger the ability")
    void nonGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        harness.setHand(player1, List.of(new IntimidatorInitiate()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Nurturer Initiate does not trigger for its own casting")
    void doesNotTriggerForItsOwnCasting() {
        harness.setHand(player1, List.of(new NurturerInitiate()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nurturer Initiate");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A trigger still targets and goes on the stack when its controller has no mana")
    void triggerDoesNotRequireManaAtTriggerTime() {
        harness.addToBattlefield(player1, new NurturerInitiate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntimidatorInitiate());

        opponentCastsGreenSpell();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }
}
