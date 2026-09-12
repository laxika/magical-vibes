package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NakayaShade.class)
class NakayaShadeTest extends BaseCardTest {

    @Test
    void getsBoostWhenNoPlayerPays() {
        Permanent shade = addShade();

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void anyPlayerCanPayToPreventTheBoost() {
        Permanent shade = addShade();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void currentPlayerCanPayBeforeOtherPlayersAreAsked() {
        Permanent shade = addShade();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyTheActivatedShadeGetsBoosted() {
        Permanent shade = addShade();
        Permanent otherShade = harness.addToBattlefieldAndReturn(player1, new NakayaShade());

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(otherShade.getEffectivePower()).isEqualTo(1);
        assertThat(otherShade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void boostsSourceEvenIfItChangesControllerBeforeResolution() {
        Permanent shade = addShade();

        activateAbilityWithoutResolving();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), shade,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, "Test setup"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent shade = addShade();

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addShade() {
        return harness.addToBattlefieldAndReturn(player1, new NakayaShade());
    }

    private void activateAbility() {
        activateAbilityWithoutResolving();
        harness.passBothPriorities();
    }

    private void activateAbilityWithoutResolving() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
    }
}
