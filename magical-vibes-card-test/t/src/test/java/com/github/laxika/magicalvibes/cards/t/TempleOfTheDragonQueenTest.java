package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.s.Spelunking;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempleOfTheDragonQueen.class, DragonEgg.class, Spelunking.class})
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
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new TempleOfTheDragonQueen());
        temple.setChosenColor(CardColor.WHITE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(temple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped and still chooses a color when no Dragon can be revealed")
    void entersTappedWithoutDragonInHand() {
        harness.setHand(player1, List.of(new TempleOfTheDragonQueen()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLACK");

        Permanent temple = findPermanent(player1, "Temple of the Dragon Queen");
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.getChosenColor()).isEqualTo(CardColor.BLACK);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Dragon does not allow untapped entry")
    void opponentsDragonDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new DragonEgg());
        harness.setHand(player2, List.of(new DragonEgg()));
        harness.setHand(player1, List.of(new TempleOfTheDragonQueen()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Temple of the Dragon Queen").isTapped()).isTrue();
    }

    @Test
    @CardUsed({TempleOfTheDragonQueen.class, Spelunking.class})
    @DisplayName("Spelunking allows untapped entry even without a Dragon")
    void spelunkingAllowsChoosingUntappedEntryWithoutDragon() {
        harness.addToBattlefield(player1, new Spelunking());
        harness.setHand(player1, List.of(new TempleOfTheDragonQueen()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "Untapped");
        harness.handleListChoice(player1, "GREEN");

        Permanent temple = findPermanent(player1, "Temple of the Dragon Queen");
        assertThat(temple.isTapped()).isFalse();
        assertThat(temple.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A revealed Dragon stays in hand and the Temple immediately produces its chosen color")
    void revealedDragonStaysInHandAndChosenManaIsAvailable(CardColor color) {
        DragonEgg dragon = new DragonEgg();
        harness.setHand(player1, List.of(new TempleOfTheDragonQueen(), dragon));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(findPermanent(player1, "Temple of the Dragon Queen").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
