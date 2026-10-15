package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PaleBears;
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

@CardUsed({BatonOfMorale.class, PaleBears.class})
class BatonOfMoraleTest extends BaseCardTest {

    @Test
    @DisplayName("Grants banding to target creature until end of turn")
    void grantsBandingToTargetCreature() {
        harness.addToBattlefield(player1, new BatonOfMorale());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Can be activated twice in a turn since it does not tap")
    void canBeActivatedRepeatedly() {
        harness.addToBattlefield(player1, new BatonOfMorale());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new BatonOfMorale());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaleBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent baton = harness.addToBattlefieldAndReturn(player1, new BatonOfMorale());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, baton.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Requires two generic mana to activate")
    void requiresTwoGenericMana() {
        harness.addToBattlefield(player1, new BatonOfMorale());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped Baton can activate using colored mana for its generic cost")
    void tappedBatonCanActivateWithColoredMana() {
        Permanent baton = harness.addToBattlefieldAndReturn(player1, new BatonOfMorale());
        baton.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();
        assertThat(baton.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability resolves and expires normally after the Baton leaves")
    void abilityResolvesAfterSourceLeaves() {
        Permanent baton = harness.addToBattlefieldAndReturn(player1, new BatonOfMorale());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(baton);
        gd.playerGraveyards.get(player1.getId()).add(baton.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("The ability does not affect another creature when its target leaves")
    void abilityDoesNotRetargetWhenTargetLeaves() {
        harness.addToBattlefield(player1, new BatonOfMorale());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, other, Keyword.BANDING)).isFalse();
    }
}
