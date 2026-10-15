package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RampagingRendhorn;
import com.github.laxika.magicalvibes.cards.r.RubblebeltRunner;
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

@CardUsed({SmeltWardIgnus.class, RubblebeltRunner.class, RampagingRendhorn.class})
class SmeltWardIgnusTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Smelt-Ward Ignus temporarily steals a creature with power 3 or less")
    void sacrificesAndStealsTarget() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new RubblebeltRunner());
        target.tap();
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Smelt-Ward Ignus");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId).contains(target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId).doesNotContain(target.getId());
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at the end of the turn")
    void controlAndHasteExpireAtEndOfTurn() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new RubblebeltRunner());
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Rubblebelt Runner");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId).contains(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId).doesNotContain(target.getId());
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetCreatureWithPowerGreaterThanThree() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new RampagingRendhorn());
        prepareForSorcerySpeedActivation();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void requiresSorcerySpeed() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new RubblebeltRunner());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Can untap and grant haste to a creature already controlled by the activator")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player1, new RubblebeltRunner());
        target.tap();
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Smelt-Ward Ignus");
        harness.assertOnBattlefield(player1, "Rubblebelt Runner");
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability does nothing if the target's power exceeds 3 before resolution")
    void rechecksPowerOnResolution() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new RubblebeltRunner());
        target.tap();
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        target.setPowerModifier(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Smelt-Ward Ignus");
        harness.assertOnBattlefield(player2, "Rubblebelt Runner");
        harness.assertNotOnBattlefield(player1, "Rubblebelt Runner");
        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Ignus may target itself and is sacrificed before resolution")
    void canSacrificeTappedSummoningSickSourceTargetingItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SmeltWardIgnus());
        source.setSummoningSick(true);
        source.tap();
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, source.getId());

        harness.assertInGraveyard(player1, "Smelt-Ward Ignus");
        harness.assertNotOnBattlefield(player1, "Smelt-Ward Ignus");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Smelt-Ward Ignus");
        harness.assertNotOnBattlefield(player2, "Smelt-Ward Ignus");
    }

    @Test
    @DisplayName("Uses current power rather than printed power when choosing a target")
    void canTargetCreatureWhosePowerWasReducedToThree() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new RampagingRendhorn());
        target.setPowerModifier(-1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rampaging Rendhorn");
        harness.assertNotOnBattlefield(player2, "Rampaging Rendhorn");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void prepareForSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
