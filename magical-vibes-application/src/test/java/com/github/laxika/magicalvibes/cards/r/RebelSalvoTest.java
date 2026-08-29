package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HauntedPlateMail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RebelSalvoTest extends BaseCardTest {

    @Test
    void affinityForEquipmentReducesCostAndRemovesIndestructibleAfterDealingDamage() {
        harness.addToBattlefield(player1, new HauntedPlateMail());
        harness.addToBattlefield(player2, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Darksteel Gargoyle"));
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Darksteel Gargoyle");
        harness.assertInGraveyard(player2, "Darksteel Gargoyle");
    }

    @Test
    void affinityCountsOnlyEquipmentControlledByTheSpellController() {
        harness.addToBattlefield(player2, new HauntedPlateMail());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
