package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CrossroadsVillage.class)
class CrossroadsVillageTest extends BaseCardTest {

    @Test
    void entersTappedAndStoresChosenColor() {
        harness.setHand(player1, List.of(new CrossroadsVillage()));

        harness.playLand(player1, 0);

        Permanent village = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(village.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(village.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    void tappingAddsOneManaOfTheChosenColor() {
        Permanent village = addReadyVillage(player1, CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(village.isTapped()).isTrue();
    }

    private Permanent addReadyVillage(Player player, CardColor chosenColor) {
        Permanent village = new Permanent(new CrossroadsVillage());
        village.setSummoningSick(false);
        village.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(village);
        return village;
    }
}
