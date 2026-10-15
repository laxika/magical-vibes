package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RetroMutation.class, SerraAngel.class, FountainOfYouth.class})
class RetroMutationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castRetroMutation(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.TURTLE);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    void enchantedCreatureCannotAttack() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RetroMutation());
        aura.setAttachedTo(angel.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RetroMutation());
        aura.setAttachedTo(angel.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RetroMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        castRetroMutation(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> angel.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void countersStillModifyTheNewBaseStats() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castRetroMutation(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
    }

    @Test
    void enchantedCreatureCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new SerraAngel());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castRetroMutation(blocker);
        declareAttackersAndPrepareBlockers(List.of(0));
        Permanent attackerAura = harness.addToBattlefieldAndReturn(player2, new RetroMutation());
        attackerAura.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        assertThat(blocker.isBlocking()).isTrue();
    }
    private void castRetroMutation(Permanent target) {
        harness.setHand(player1, List.of(new RetroMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
