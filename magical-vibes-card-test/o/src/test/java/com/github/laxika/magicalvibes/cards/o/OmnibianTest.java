package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AzoriusChancery;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Omnibian.class, MistralCharger.class, AzoriusChancery.class})
class OmnibianTest extends BaseCardTest {

    @Test
    @DisplayName("Makes a target creature a 3/3 Frog until end of turn")
    void makesTargetCreatureAThreeThreeFrog() {
        addReadyOmnibian();
        Permanent target = addCreatureReady(player2, new MistralCharger());

        activateOmnibian(target);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.FROG);
    }

    @Test
    @DisplayName("The Frog and base power and toughness changes expire at end of turn")
    void changesExpireAtEndOfTurn() {
        addReadyOmnibian();
        Permanent target = addCreatureReady(player2, new MistralCharger());

        activateOmnibian(target);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.PEGASUS);
    }

    @Test
    @DisplayName("Pays the tap cost when activated")
    void paysTapCostWhenActivated() {
        Permanent omnibian = addReadyOmnibian();
        Permanent target = addCreatureReady(player2, new MistralCharger());

        activateOmnibian(target);

        assertThat(omnibian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyOmnibian();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AzoriusChancery());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Activates with only a tap and no mana")
    void activatesWithoutMana() {
        Permanent omnibian = addReadyOmnibian();
        Permanent target = addCreatureReady(player2, new MistralCharger());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(omnibian.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.FROG);
    }

    @Test
    @DisplayName("Changing base stats and creature type preserves flying and counters")
    void preservesAbilitiesAndCounters() {
        addReadyOmnibian();
        Permanent target = addCreatureReady(player2, new MistralCharger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateOmnibian(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.FROG);
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent omnibian = addReadyOmnibian();

        activateOmnibian(omnibian);

        assertThat(omnibian.isTapped()).isTrue();
        assertThat(omnibian.getEffectivePower()).isEqualTo(3);
        assertThat(omnibian.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, omnibian)).containsExactly(CardSubtype.FROG);
    }
    private void activateOmnibian(Permanent target) {
        addManaForAbility();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyOmnibian() {
        return addCreatureReady(player1, new Omnibian());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
