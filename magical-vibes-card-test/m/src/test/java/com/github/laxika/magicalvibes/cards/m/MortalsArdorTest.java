package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MortalsArdor.class, NyxbornWolf.class, SpringleafDrum.class})
class MortalsArdorTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink gains life from the boosted creature's combat damage")
    void gainsLifeFromCombatDamage() {
        Permanent wolf = addCreatureReady(player1, new NyxbornWolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MortalsArdor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An opponent's targeted creature gains life for its controller")
    void opponentReceivesLifelinkLifeGain() {
        Permanent wolf = addCreatureReady(player2, new NyxbornWolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MortalsArdor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();
        assertThat(wolf.getPowerModifier()).isEqualTo(1);
        assertThat(wolf.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.LIFELINK)).isTrue();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Repeated casts stack the boost but do not multiply lifelink")
    void repeatedCastsDoNotMultiplyLifelink() {
        Permanent wolf = addCreatureReady(player1, new NyxbornWolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MortalsArdor(), new MortalsArdor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();
        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A departed target does not transfer either effect to another creature")
    void departedTargetDoesNotTransferEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        harness.setHand(player1, List.of(new MortalsArdor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mortal's Ardor");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Grants +1/+1 and lifelink to target creature")
    void grantsBoostAndLifelink() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        harness.setHand(player1, List.of(new MortalsArdor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = bear.getId();
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Boost and lifelink wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        harness.setHand(player1, List.of(new MortalsArdor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = bear.getId();
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        harness.setHand(player1, List.of(new MortalsArdor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = drum.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
