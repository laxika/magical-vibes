package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CetaSanctuary;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnaDisciple.class, AngelfireCrusader.class, CetaSanctuary.class})
class AnaDiscipleTest extends BaseCardTest {

    @Test
    void givesTargetCreatureFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new AnaDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void weakensTargetCreatureUntilEndOfTurn() {
        addCreatureReady(player1, new AnaDisciple());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void abilitiesCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new AnaDisciple());
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player2, new CetaSanctuary());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sanctuary.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothAbilitiesCanTargetTheDiscipleItself(int abilityIndex) {
        Permanent disciple = addCreatureReady(player1, new AnaDisciple());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.BLUE : ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, disciple.getId());

        assertThat(disciple.isTapped()).isTrue();
        assertThat(disciple.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(disciple.getPowerModifier()).isZero();

        harness.passBothPriorities();

        if (abilityIndex == 0) {
            assertThat(disciple.hasKeyword(Keyword.FLYING)).isTrue();
        } else {
            assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(-1);
            assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(1);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(disciple);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void neitherAbilityCanBeActivatedWhileSummoningSick(int abilityIndex) {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new AnaDisciple());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.BLUE : ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void neitherAbilityCanBeActivatedWhileTapped(int abilityIndex) {
        Permanent disciple = addCreatureReady(player1, new AnaDisciple());
        disciple.setTapped(true);
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.BLUE : ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void neitherAbilityCanBePaidWithTheOtherColor(int abilityIndex) {
        Permanent disciple = addCreatureReady(player1, new AnaDisciple());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.BLACK : ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blackAbilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new AnaDisciple());
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player2, new CetaSanctuary());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, sanctuary.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
