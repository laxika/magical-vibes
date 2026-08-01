package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelesnyaGuildgateTest extends BaseCardTest {

    @Test
    @DisplayName("Selesnya Guildgate enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SelesnyaGuildgate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent guildgate = findPermanent(player1, "Selesnya Guildgate");
        assertThat(guildgate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between green and white")
    void activatingPromptsColorChoice() {
        addGuildgateReady(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color and taps the land")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"GREEN", "WHITE"}) {
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
        Permanent perm = new Permanent(new SelesnyaGuildgate());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
