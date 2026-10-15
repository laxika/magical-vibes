package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void chosenColorFromEntryDeterminesManaWithoutUsingStack(CardColor color) {
        harness.setHand(player1, List.of(new CrossroadsVillage()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        Permanent village = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color.name())))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(village.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void separateVillagesKeepIndependentColorChoices() {
        harness.setHand(player1, List.of(new CrossroadsVillage(), new CrossroadsVillage()));
        harness.playLand(player1, 0);
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handleListChoice(player1, "WHITE");
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.playLand(player1, 0);
        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.handleListChoice(player1, "BLACK");

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    private Permanent addReadyVillage(Player player, CardColor chosenColor) {
        Permanent village = harness.addToBattlefieldAndReturn(player, new CrossroadsVillage());
        village.setSummoningSick(false);
        village.setChosenColor(chosenColor);
        return village;
    }
}
