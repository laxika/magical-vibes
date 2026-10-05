package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PatriotShieldWielder.class, GrizzlyBears.class, FountainOfYouth.class})
class PatriotShieldWielderTest extends BaseCardTest {

    @Test
    @DisplayName("Gives another creature +2/+0 and hexproof until end of turn")
    void givesAnotherCreatureBoostAndHexproof() {
        Permanent patriot = addCreatureReady(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(patriot.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Can target only another creature you control")
    void restrictsTargetToAnotherCreatureYouControl() {
        Permanent patriot = addCreatureReady(player1, new PatriotShieldWielder());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, patriot.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without paying two mana")
    void cannotActivateWithOnlyOneMana() {
        Permanent patriot = addCreatureReady(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(patriot.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when Patriot is already tapped")
    void cannotActivateWhileTapped() {
        Permanent patriot = addCreatureReady(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        patriot.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even if Patriot dies before resolution")
    void resolvesAfterSourceDies() {
        Permanent patriot = addCreatureReady(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());

        patriot.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(patriot);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("A target that dies before resolution receives neither effect")
    void doesNotAffectDepartedTarget() {
        addCreatureReady(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());

        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can target your own hexproof creature and the boosts accumulate")
    void canTargetOwnHexproofCreatureAgain() {
        Permanent patriot = addCreatureReady(player1, new PatriotShieldWielder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        patriot.setTapped(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(patriot.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }
}
