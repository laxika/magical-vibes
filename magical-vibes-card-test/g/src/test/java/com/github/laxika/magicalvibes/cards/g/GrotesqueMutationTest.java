package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrotesqueMutation.class, QuilledWolf.class, MagnifyingGlass.class, RabidBite.class})
class GrotesqueMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +3/+1 and lifelink to target creature")
    void grantsBoostAndLifelink() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new GrotesqueMutation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(5);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Boost and lifelink wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new GrotesqueMutation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(2);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {

        Permanent glass = harness.addToBattlefieldAndReturn(player1, new MagnifyingGlass());
        harness.setHand(player1, List.of(new GrotesqueMutation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, glass.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opposing creature gains lifelink for its controller")
    void opposingCreatureGainsLifeForItsController() {
        Permanent wolf = addCreatureReady(player2, new QuilledWolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrotesqueMutation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(5);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(3);
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Repeated mutations stack boosts without multiplying lifelink")
    void repeatedMutationsDoNotMultiplyLifelink() {
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrotesqueMutation(), new GrotesqueMutation()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(8);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Granted lifelink applies to noncombat damage")
    void gainsLifeFromNoncombatDamage() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrotesqueMutation(), new RabidBite()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, List.of(wolf.getId(), opponent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Quilled Wolf");
    }
}
