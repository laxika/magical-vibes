package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscipleOfKangeeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives target creature flying and makes it blue until end of turn")
    void grantsFlyingAndMakesBlue() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfKangee());
        disciple.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(disciple.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Granted flying and blue color wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfKangee());
        disciple.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfKangee());
        disciple.setSummoningSick(false);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
