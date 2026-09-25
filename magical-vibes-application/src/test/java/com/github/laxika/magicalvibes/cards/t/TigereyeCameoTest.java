package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TigereyeCameo.class)
class TigereyeCameoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds green mana")
    void tapForGreenMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new TigereyeCameo());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Tapping adds white mana")
    void tapForWhiteMana() {
        Permanent cameo = harness.addToBattlefieldAndReturn(player1, new TigereyeCameo());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        assertThat(cameo.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana abilities do not use the stack")
    void manaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new TigereyeCameo());
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gameData.stack).isEmpty();
        harness.handleListChoice(player1, "GREEN");
        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameData.stack).isEmpty();
    }
}
