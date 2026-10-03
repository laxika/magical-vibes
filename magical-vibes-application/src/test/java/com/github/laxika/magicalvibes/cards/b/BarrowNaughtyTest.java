package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SnaremasterSprite;
import com.github.laxika.magicalvibes.cards.v.VoraciousVermin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowNaughty.class, SnaremasterSprite.class, VoraciousVermin.class})
class BarrowNaughtyTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink only while you control another Faerie")
    void conditionalLifelink() {
        Permanent naughty = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());

        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isFalse();

        harness.addToBattlefield(player1, new VoraciousVermin());
        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isFalse();

        harness.addToBattlefield(player2, new SnaremasterSprite());
        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isFalse();

        harness.addToBattlefield(player1, new SnaremasterSprite());
        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Activated ability gives +1/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent naughty = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, naughty)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, naughty)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, naughty)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, naughty)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses lifelink immediately when the other Faerie leaves")
    void losesLifelinkWhenOtherFaerieLeaves() {
        Permanent naughty = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());

        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isTrue();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other));

        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations work while tapped and their boosts accumulate")
    void repeatedActivationsWhileTapped() {
        Permanent naughty = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());
        naughty.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, naughty)).isEqualTo(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, naughty)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, naughty)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, naughty)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, naughty)).isEqualTo(3);
    }

    @Test
    @DisplayName("Conditional lifelink gains life equal to boosted combat damage")
    void gainsLifeFromBoostedCombatDamage() {
        Permanent naughty = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());
        harness.addToBattlefield(player1, new SnaremasterSprite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        naughty.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
