package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoltenTributary.class)
class MoltenTributaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new MoltenTributary()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability prompts for blue or red")
    void manaAbilityPromptsForBlueOrRed() {
        addReadyTributary(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gameData = harness.getGameData();
        PendingInteraction.ColorChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "RED");
    }

    @Test
    void choosingBlueAddsOneBlueMana() {
        Permanent tributary = addReadyTributary(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tributary.isTapped()).isTrue();
    }

    @Test
    void choosingRedAddsOneRedMana() {
        Permanent tributary = addReadyTributary(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(tributary.isTapped()).isTrue();
    }

    private Permanent addReadyTributary(Player player) {
        Permanent permanent = new Permanent(new MoltenTributary());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
