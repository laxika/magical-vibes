package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShacklesTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = enchantOpponentBears();
        bears.tap();

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability returns Shackles to its owner's hand")
    void activatedAbilityReturnsSelfToHand() {
        Permanent bears = enchantOpponentBears();
        int auraIndex = findPermanentIndex(player1, "Shackles");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shackles");
        harness.assertInHand(player1, "Shackles");
    }

    private Permanent enchantOpponentBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shackles()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Shackles");
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        return bears;
    }

    private int findPermanentIndex(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        var battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        return -1;
    }
}
