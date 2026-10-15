package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirdochCore.class})
class FirdochCoreTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Firdoch Core adds one mana of the chosen color")
    void tappingAddsChosenMana() {
        Permanent core = addCreatureReady(player1, new FirdochCore());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(core.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four mana makes Firdoch Core a 4/4 creature")
    void payingFourManaAnimatesCore() {
        Permanent core = addCreatureReady(player1, new FirdochCore());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, core)).isTrue();
        assertThat(gqs.getEffectivePower(gd, core)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, core)).isEqualTo(4);
    }

    @Test
    @DisplayName("Firdoch Core stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent core = addCreatureReady(player1, new FirdochCore());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, core)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(core.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, core)).isFalse();
    }

    @Test
    @DisplayName("A newly entered noncreature Core can tap for mana")
    void newlyEnteredCoreCanProduceMana() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new FirdochCore());
        core.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(core.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Animation can be activated while Core is tapped and does not untap it")
    void tappedCoreCanBeAnimated() {
        Permanent core = addCreatureReady(player1, new FirdochCore());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gqs.isCreature(gd, core)).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(core.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, core)).isTrue();
        assertThat(gqs.isArtifact(gd, core)).isTrue();
        assertThat(gqs.getEffectivePower(gd, core)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, core)).isEqualTo(4);
    }

    @Test
    @DisplayName("Animation retains the mana ability on a Core controlled since turn start")
    void animatedReadyCoreCanProduceMana() {
        Permanent core = addCreatureReady(player1, new FirdochCore());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(core.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, core)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered Core can animate but then cannot tap for mana")
    void newlyEnteredAnimatedCoreHasSummoningSickness() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new FirdochCore());
        core.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, core)).isTrue();
        assertThat(core.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
