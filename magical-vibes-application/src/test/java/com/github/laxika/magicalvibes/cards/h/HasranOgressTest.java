package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HasranOgressTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking without paying deals 3 damage to its controller")
    void attackingWithoutPayingDealsDamageToController() {
        addCreatureReady(player1, new HasranOgress());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(java.util.List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Paying {2} prevents the attack trigger damage")
    void payingManaPreventsDamage() {
        addCreatureReady(player1, new HasranOgress());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(java.util.List.of(0));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
