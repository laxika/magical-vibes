package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Farmstead.class, Forest.class, GrizzlyBears.class})
class FarmsteadTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's controller may pay to gain 1 life during upkeep")
    void payingDuringEnchantedLandsControllerUpkeepGainsLife() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        castFarmstead(land);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Declining Farmstead's upkeep payment gains no life")
    void decliningDuringUpkeepGainsNoLife() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castFarmstead(land);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Farmstead can enchant only a land")
    void onlyTargetsLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Farmstead()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private void castFarmstead(Permanent land) {
        harness.setHand(player1, List.of(new Farmstead()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
    }
}
