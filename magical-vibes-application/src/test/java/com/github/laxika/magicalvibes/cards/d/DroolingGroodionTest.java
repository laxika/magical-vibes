package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroolingGroodion.class, GrizzlyBears.class})
class DroolingGroodionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, then pumps the first target and weakens the second")
    void sacrificesAndModifiesBothTargets() {
        addCreatureReady(player1, new DroolingGroodion());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drooling Groodion");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(firstTarget.getPowerModifier()).isEqualTo(2);
        assertThat(firstTarget.getToughnessModifier()).isEqualTo(2);
        assertThat(secondTarget.getPowerModifier()).isEqualTo(-2);
        assertThat(secondTarget.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Cannot target the same creature for both target groups")
    void targetsMustBeDistinct() {
        addCreatureReady(player1, new DroolingGroodion());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("-2/-2 kills a 2/2 creature")
    void debuffKillsCreature() {
        addCreatureReady(player1, new DroolingGroodion());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
