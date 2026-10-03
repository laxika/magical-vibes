package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({AspirantsAscent.class, Cankerbloom.class, Forest.class})
class AspirantsAscentTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+3 and gains flying and toxic")
    void boostsAndGrantsKeywords() {
        Permanent target = castAspirantsAscent(new Cankerbloom());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TOXIC)).isTrue();
    }

    @Test
    @DisplayName("Toxic 1 gives one poison counter when the creature deals combat damage")
    void toxicGivesPoisonCounter() {
        Permanent target = castAspirantsAscent(new Cankerbloom());
        target.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Boost, flying, and toxic wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = castAspirantsAscent(new Cankerbloom());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TOXIC)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AspirantsAscent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Toxic gives poison as part of combat damage without using the stack")
    void toxicDoesNotUseStack() {
        Permanent target = castAspirantsAscent(new Cankerbloom());
        target.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can grant the effects to an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cankerbloom());
        harness.setHand(player1, List.of(new AspirantsAscent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        target.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The granted toxic no longer gives poison after the turn ends")
    void toxicDamageEffectExpires() {
        Permanent target = castAspirantsAscent(new Cankerbloom());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        target.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    private Permanent castAspirantsAscent(Cankerbloom targetCard) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        harness.setHand(player1, List.of(new AspirantsAscent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        return target;
    }
}
