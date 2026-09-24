package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CranialPlating;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianLorebearers.class, CranialPlating.class, SkyhunterProwler.class})
class ViridianLorebearersTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature by the number of artifacts opponents control")
    void boostsByOpponentArtifacts() {
        addCreatureReady(player1, new ViridianLorebearers());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkyhunterProwler());
        harness.addToBattlefield(player1, new CranialPlating());
        harness.addToBattlefield(player2, new CranialPlating());
        harness.addToBattlefield(player2, new CranialPlating());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts artifacts when the ability resolves")
    void countsAtResolution() {
        addCreatureReady(player1, new ViridianLorebearers());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkyhunterProwler());
        harness.addToBattlefield(player2, new CranialPlating());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.addToBattlefield(player2, new CranialPlating());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        addCreatureReady(player1, new ViridianLorebearers());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkyhunterProwler());
        harness.addToBattlefield(player2, new CranialPlating());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new ViridianLorebearers());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CranialPlating());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Pays {3}{G} and taps itself to activate")
    void paysManaAndTapsSource() {
        Permanent source = addCreatureReady(player1, new ViridianLorebearers());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkyhunterProwler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ViridianLorebearers());
        Permanent target = addCreatureReady(player2, new SkyhunterProwler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent source = addCreatureReady(player1, new ViridianLorebearers());
        source.tap();
        Permanent target = addCreatureReady(player2, new SkyhunterProwler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new ViridianLorebearers());
        Permanent target = addCreatureReady(player2, new SkyhunterProwler());
        harness.addToBattlefield(player2, new CranialPlating());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives no boost when opponents control no artifacts")
    void givesNoBoostWithoutOpponentArtifacts() {
        addCreatureReady(player1, new ViridianLorebearers());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkyhunterProwler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }
}
