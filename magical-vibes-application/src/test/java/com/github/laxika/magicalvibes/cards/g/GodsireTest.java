package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GodsireTest extends BaseCardTest {

    private long beastTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Beast") && p.getCard().isToken())
                .count();
    }

    @Test
    @DisplayName("Tapping creates an 8/8 Beast token")
    void createsBeastToken() {
        addCreatureReady(player1, new Godsire());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(beastTokenCount()).isEqualTo(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Beast") && p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(8);
        assertThat(token.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new Godsire());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(beastTokenCount()).isEqualTo(1);
    }
}
