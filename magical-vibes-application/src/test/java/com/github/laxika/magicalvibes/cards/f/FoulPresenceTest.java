package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoulPresence.class, GrizzlyBears.class, FountainOfYouth.class})
class FoulPresenceTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsMinusOneMinusOne() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(enchantedCreature);

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(1);
    }

    @Test
    void enchantedCreatureCanTapToGiveTargetCreatureMinusOneMinusOneUntilEndOfTurn() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(enchantedCreature);
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(1);
        assertThat(enchantedCreature.isTapped()).isTrue();

        forceEndStep();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(2);
    }

    @Test
    void grantedAbilityCannotTargetNoncreaturePermanent() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(enchantedCreature);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void abilityIsLostWhenAuraLeavesTheBattlefield() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAttachedAura(enchantedCreature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addAttachedAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FoulPresence());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }

    private void forceEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
