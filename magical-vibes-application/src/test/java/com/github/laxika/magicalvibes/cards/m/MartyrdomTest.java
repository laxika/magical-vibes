package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.d.DeathSpark;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Martyrdom.class, DeathSpark.class, StormCrow.class})
class MartyrdomTest extends BaseCardTest {

    private Permanent addProtectedCreature() {
        return addCreatureReady(player1, new StormCrow());
    }

    private void castMartyrdom(Permanent target) {
        harness.setHand(player1, List.of(new Martyrdom()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void castDeathSpark(UUID targetId) {
        harness.setHand(player1, List.of(new DeathSpark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    void redirectsDamageToPlayer() {
        Permanent protectedCreature = addProtectedCreature();
        castMartyrdom(protectedCreature);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        castDeathSpark(player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void redirectsDamageToAnotherCreature() {
        Permanent protectedCreature = addProtectedCreature();
        Permanent otherCreature = addCreatureReady(player2, new StormCrow());
        castMartyrdom(protectedCreature);

        harness.activateAbility(player1, 0, null, otherCreature.getId());
        harness.passBothPriorities();

        castDeathSpark(otherCreature.getId());

        assertThat(otherCreature.getMarkedDamage()).isEqualTo(0);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void redirectsOnlyTheNextDamage() {
        Permanent protectedCreature = addProtectedCreature();
        castMartyrdom(protectedCreature);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        castDeathSpark(player1.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);

        castDeathSpark(player1.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTargetAnOpponentsCreature() {
        addProtectedCreature();
        Permanent opponentsCreature = addCreatureReady(player2, new StormCrow());
        harness.setHand(player1, List.of(new Martyrdom()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentsCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent protectedCreature = addProtectedCreature();
        castMartyrdom(protectedCreature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canRedirectDamageToTheProtectedCreatureItself() {
        Permanent protectedCreature = addProtectedCreature();
        castMartyrdom(protectedCreature);

        harness.activateAbility(player1, 0, null, protectedCreature.getId());
        harness.passBothPriorities();

        castDeathSpark(protectedCreature.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @CardUsed(AjaniGoldmane.class)
    void redirectsDamageToAPlaneswalker() {
        Permanent protectedCreature = addProtectedCreature();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        castMartyrdom(protectedCreature);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        castDeathSpark(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void onlyTheCasterCanActivateTheGrantedAbilityAfterControlChanges() {
        Permanent protectedCreature = addProtectedCreature();
        castMartyrdom(protectedCreature);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(
                        gd,
                        player2.getId(),
                        protectedCreature,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT,
                        null,
                        "Test setup"));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
