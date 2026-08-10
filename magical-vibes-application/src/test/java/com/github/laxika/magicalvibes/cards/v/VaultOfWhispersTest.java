package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaultOfWhispersTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Vault of Whispers adds black mana")
    void tapForBlackMana() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new VaultOfWhispers());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
