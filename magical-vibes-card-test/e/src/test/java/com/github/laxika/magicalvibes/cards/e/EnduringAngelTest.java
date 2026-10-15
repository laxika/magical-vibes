package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduringAngel.class, AngelicEnforcer.class, PlayWithFire.class, PlatinumAngel.class})
class EnduringAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms and sets its controller's life to 3 instead of a lethal life reduction")
    void replacesLifeLoss() {
        Permanent angel = addReadyAngel(player1);
        harness.setLife(player1, 1);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 2, "life loss"));

        harness.runStateBasedActions();

        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        assertThat(angel.isTransformed()).isTrue();
        assertThat(angel.getCard()).isInstanceOf(AngelicEnforcer.class);
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Angelic Enforcer's power and toughness equal its controller's life")
    void backFacePowerToughnessEqualsLife() {
        Permanent angel = addTransformedAngel(player1);
        harness.setLife(player1, 7);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(7);
    }

    @Test
    @DisplayName("Angelic Enforcer doubles its controller's life when it attacks")
    void doublesLifeWhenAttacking() {
        addTransformedAngel(player1);
        harness.setLife(player1, 7);
        declareAttackers(List.of(0));

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    private Permanent addReadyAngel(Player player) {
        return addCreatureReady(player, new EnduringAngel());
    }

    private Permanent addTransformedAngel(Player player) {
        EnduringAngel card = new EnduringAngel();
        Permanent angel = addCreatureReady(player, card);
        angel.setCard(card.getBackFaceCard());
        angel.setTransformed(true);
        return angel;
    }

    @Test
    void frontFacePreventsOpponentsFromTargetingController() {
        addReadyAngel(player1);
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void backFacePreventsOpponentsFromTargetingController() {
        addTransformedAngel(player1);
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllerHexproofDoesNotProtectTheCreature() {
        Permanent angel = addReadyAngel(player1);
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, angel.getId());

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    void lethalReductionIsReplacedBeforeSubsequentLifeGain() {
        Permanent angel = addReadyAngel(player1);
        harness.setLife(player1, 1);

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 2, "life loss");
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        });
        harness.runStateBasedActions();

        harness.assertLife(player1, 4);
        assertThat(angel.isTransformed()).isTrue();
        assertThat(gd.lifeGainedThisTurn.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void replacementUsesLifeBeforeTheLethalReduction() {
        addReadyAngel(player1);
        harness.setLife(player1, 5);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 7, "life loss"));
        harness.runStateBasedActions();

        harness.assertLife(player1, 3);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void nonlethalLifeLossDoesNotTransform() {
        Permanent angel = addReadyAngel(player1);
        harness.setLife(player1, 5);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 2, "life loss"));
        harness.runStateBasedActions();

        harness.assertLife(player1, 3);
        assertThat(angel.isTransformed()).isFalse();
    }

    @Test
    void backFaceDoesNotReplaceAnotherLethalReduction() {
        addTransformedAngel(player1);
        harness.setLife(player1, 3);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 3, "life loss"));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    void doublesNegativeLifeByLosingLife() {
        Permanent angel = addTransformedAngel(player1);
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, -2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, -4);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }
}
