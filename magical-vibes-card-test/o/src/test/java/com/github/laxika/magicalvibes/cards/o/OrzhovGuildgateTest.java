package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrzhovGuildgate.class})
class OrzhovGuildgateTest extends BaseCardTest {

    @Test
    @DisplayName("Orzhov Guildgate enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new OrzhovGuildgate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent guildgate = findPermanent(player1, "Orzhov Guildgate");
        assertThat(guildgate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between white and black")
    void activatingPromptsColorChoice() {
        addGuildgateReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLACK");
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"WHITE", "BLACK"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent guildgate = addGuildgateReady(player1);
            GameData gd = harness.getGameData();
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(guildgate.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    private Permanent addGuildgateReady(Player player) {
        Permanent guildgate = harness.addToBattlefieldAndReturn(player, new OrzhovGuildgate());
        guildgate.setSummoningSick(false);
        return guildgate;
    }

    @Test
    @DisplayName("Orzhov Guildgate enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent guildgate = harness.enterBattlefieldAndReturn(player1, new OrzhovGuildgate());

        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Guildgate cannot activate its mana ability")
    void tappedLandCannotProduceMana() {
        Permanent guildgate = addGuildgateReady(player1);
        guildgate.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A newly controlled noncreature Guildgate can produce mana once untapped")
    void newlyControlledLandCanProduceMana() {
        Permanent guildgate = harness.enterBattlefieldAndReturn(player1, new OrzhovGuildgate());
        guildgate.setSummoningSick(true);
        guildgate.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(guildgate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
