package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrzasPowerPlantTest extends BaseCardTest {

    // ===== Conditional mana ability =====

    @Test
    @DisplayName("Tapping alone adds one colorless mana")
    void tapAloneAddsOne() {
        harness.addToBattlefield(player1, new UrzasPowerPlant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping with a Mine and a Tower adds two colorless mana")
    void tapWithFullTronAddsTwo() {
        harness.addToBattlefield(player1, new UrzasPowerPlant());
        harness.addToBattlefield(player1, named("Urza's Mine"));
        harness.addToBattlefield(player1, named("Urza's Tower"));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping with only a Mine adds one colorless mana")
    void tapWithPartialTronAddsOne() {
        harness.addToBattlefield(player1, new UrzasPowerPlant());
        harness.addToBattlefield(player1, named("Urza's Mine"));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Card named(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
