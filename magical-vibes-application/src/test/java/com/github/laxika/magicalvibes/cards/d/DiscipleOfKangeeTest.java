package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscipleOfKangee.class, GrizzlyBears.class, FountainOfYouth.class})
class DiscipleOfKangeeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives target creature flying and makes it blue until end of turn")
    void grantsFlyingAndMakesBlue() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfKangee());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(disciple.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Granted flying and blue color wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        addCreatureReady(player1, new DiscipleOfKangee());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Ability can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfKangee());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(disciple.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Ability requires blue mana")
    void requiresBlueMana() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfKangee());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(disciple.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new DiscipleOfKangee());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Disciple can target itself")
    void canTargetItself() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfKangee());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, disciple.getId());
        harness.passBothPriorities();

        assertThat(disciple.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, disciple, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, disciple)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("A summoning sick Disciple cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfKangee());
        disciple.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Disciple cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfKangee());
        disciple.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after Disciple leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfKangee());
        Permanent target = addCreatureReady(player2, new DiscipleOfKangee());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, disciple);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Ability does not affect a creature that left and returned before resolution")
    void doesNotAffectReturnedTarget() {
        addCreatureReady(player1, new DiscipleOfKangee());
        DiscipleOfKangee targetCard = new DiscipleOfKangee();
        Permanent target = addCreatureReady(player2, targetCard);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        gd.playerHands.get(player2.getId()).remove(targetCard);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, returned)).containsExactly(CardColor.WHITE);
    }
}
