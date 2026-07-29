package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JunglePatrolTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 0/1 green Wall token with defender named Wood")
    void createsWoodToken() {
        addJunglePatrol(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wood = woodToken(player1);
        assertThat(wood).isNotNull();
        assertThat(wood.getCard().getPower()).isEqualTo(0);
        assertThat(wood.getCard().getToughness()).isEqualTo(1);
        assertThat(wood.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(wood.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Second ability sacrifices a Wood token to add {R}")
    void sacrificesWoodForRedMana() {
        addJunglePatrol(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(woodToken(player1)).isNotNull();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(woodToken(player1)).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability cannot be activated without a Wood token")
    void requiresWoodToken() {
        addJunglePatrol(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addJunglePatrol(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JunglePatrol());
        perm.setSummoningSick(false);
    }

    private Permanent woodToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Wood".equals(p.getCard().getName()))
                .findFirst()
                .orElse(null);
    }
}
