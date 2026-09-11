package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleOfTheDragonQueen.class, DragonEgg.class})
class TempleOfTheDragonQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Dragon and decline to reveal one")
    void entersTappedWhenDecliningDragonReveal() {
        harness.setHand(player1, List.of(new TempleOfTheDragonQueen(), new DragonEgg()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleListChoice(player1, "BLUE");

        Permanent temple = findPermanent(player1, "Temple of the Dragon Queen");
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Enters untapped when you reveal a Dragon")
    void entersUntappedWhenRevealingDragon() {
        harness.setHand(player1, List.of(new TempleOfTheDragonQueen(), new DragonEgg()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "RED");

        Permanent temple = findPermanent(player1, "Temple of the Dragon Queen");
        assertThat(temple.isTapped()).isFalse();
        assertThat(temple.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Enters untapped without a reveal when you control a Dragon")
    void entersUntappedWhenControllingDragon() {
        harness.addToBattlefield(player1, new DragonEgg());
        harness.setHand(player1, List.of(new TempleOfTheDragonQueen()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        Permanent temple = findPermanent(player1, "Temple of the Dragon Queen");
        assertThat(temple.isTapped()).isFalse();
        assertThat(temple.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tappingAddsChosenColorMana() {
        Permanent temple = new Permanent(new TempleOfTheDragonQueen());
        temple.setSummoningSick(false);
        temple.setChosenColor(CardColor.WHITE);
        gd.playerBattlefields.get(player1.getId()).add(temple);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(temple.isTapped()).isTrue();
    }
}
