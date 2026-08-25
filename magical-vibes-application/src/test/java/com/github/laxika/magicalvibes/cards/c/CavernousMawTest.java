package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavernousMaw.class, CavernOfSouls.class})
class CavernousMawTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Cavernous Maw produces colorless mana")
    void tappingProducesColorlessMana() {
        harness.addToBattlefield(player1, new CavernousMaw());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isOne();
    }

    @Test
    @DisplayName("The animation ability counts other Caves and Cave cards in the graveyard")
    void animationCountsOtherCavesAndGraveyardCaves() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new CavernousMaw());
        harness.addToBattlefield(player1, new CavernOfSouls());
        harness.setGraveyard(player1, List.of(new CavernOfSouls(), new CavernOfSouls()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, maw)).isTrue();
        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(3);
        assertThat(gqs.isLand(gd, maw)).isTrue();
        assertThat(maw.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("The source Cave is not counted as an other Cave")
    void sourceCaveIsNotCounted() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new CavernousMaw());
        harness.setGraveyard(player1, List.of(new CavernOfSouls(), new CavernOfSouls()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or greater");
        assertThat(maw.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new CavernousMaw());
        harness.addToBattlefield(player1, new CavernOfSouls());
        harness.addToBattlefield(player1, new CavernOfSouls());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, maw)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, maw)).isFalse();
        assertThat(gqs.isLand(gd, maw)).isTrue();
    }
}
