package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TigereyeCameoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds green mana")
    void tapForGreenMana() {
        harness.addToBattlefield(player1, new TigereyeCameo());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent cameo = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds white mana")
    void tapForWhiteMana() {
        harness.addToBattlefield(player1, new TigereyeCameo());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana abilities do not use the stack")
    void manaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new TigereyeCameo());
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }
}
