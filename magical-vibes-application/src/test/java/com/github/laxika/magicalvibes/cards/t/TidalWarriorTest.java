package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.StrongholdAssassin;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalWarrior.class, VolrathsStronghold.class, StrongholdAssassin.class})
class TidalWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability targets a land")
    void activatingAbilityTargetsLand() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(stronghold.getId());
    }

    @Test
    @DisplayName("Activating the ability can target an opponent's land")
    void activatingAbilityCanTargetOpponentsLand() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(stronghold.getId());
    }

    @Test
    @DisplayName("Resolving the ability makes the target land an Island")
    void landBecomesIsland() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, stronghold)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.hasLostPrintedAbilities(gd, stronghold)).isTrue();
        assertThat(gqs.getOverriddenLandManaColor(gd, stronghold)).isEqualTo(ManaColor.BLUE);
    }

    @Test
    @DisplayName("The granted Island type wears off at end of turn")
    void islandTypeWearsOff() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, stronghold)).isEmpty();
        assertThat(gqs.hasLostPrintedAbilities(gd, stronghold)).isFalse();
        assertThat(gqs.getOverriddenLandManaColor(gd, stronghold)).isNull();
    }

    @Test
    @DisplayName("The ability cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent assassin = addCreatureReady(player1, new StrongholdAssassin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, assassin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
