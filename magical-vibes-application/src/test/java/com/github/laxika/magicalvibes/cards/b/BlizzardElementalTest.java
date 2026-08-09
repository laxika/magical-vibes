package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlizzardElementalTest extends BaseCardTest {

    @Test
    @DisplayName("{3}{U} ability untaps Blizzard Elemental")
    void untapAbilityUntapsSelf() {
        Permanent elemental = addReadyElemental(player1);
        elemental.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isFalse();
    }

    private Permanent addReadyElemental(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BlizzardElemental());
        perm.setSummoningSick(false);
        return perm;
    }
}
