package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpurredWolverine.class, SpikedBaloth.class, AirElemental.class, Pacifism.class})
class SpurredWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two Beasts gives the target creature first strike until end of turn")
    void grantsFirstStrikeToTargetCreature() {
        Permanent wolverine = addCreatureReady(player1, new SpurredWolverine());
        Permanent beast1 = addCreatureReady(player1, new SpikedBaloth());
        Permanent beast2 = addCreatureReady(player1, new SpikedBaloth());
        Permanent target = addCreatureReady(player2, new AirElemental());

        activateAbility(wolverine, beast1, beast2, target);
        harness.passBothPriorities();

        assertThat(beast1.isTapped()).isTrue();
        assertThat(beast2.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent wolverine = addCreatureReady(player1, new SpurredWolverine());
        Permanent beast1 = addCreatureReady(player1, new SpikedBaloth());
        Permanent beast2 = addCreatureReady(player1, new SpikedBaloth());
        Permanent target = addCreatureReady(player2, new AirElemental());

        activateAbility(wolverine, beast1, beast2, target);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without two untapped Beasts")
    void requiresTwoUntappedBeasts() {
        Permanent wolverine = addCreatureReady(player1, new SpurredWolverine());
        Permanent nonBeast = addCreatureReady(player1, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(wolverine), null, wolverine.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nonBeast.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void requiresCreatureTarget() {
        Permanent wolverine = addCreatureReady(player1, new SpurredWolverine());
        Permanent beast1 = addCreatureReady(player1, new SpikedBaloth());
        Permanent beast2 = addCreatureReady(player1, new SpikedBaloth());
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(wolverine), null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(beast1.isTapped()).isFalse();
        assertThat(beast2.isTapped()).isFalse();
    }

    private void activateAbility(Permanent wolverine, Permanent beast1, Permanent beast2, Permanent target) {
        harness.activateAbility(player1, battlefieldIndex(wolverine), null, target.getId());
        harness.handlePermanentChosen(player1, beast1.getId());
        harness.handlePermanentChosen(player1, beast2.getId());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
