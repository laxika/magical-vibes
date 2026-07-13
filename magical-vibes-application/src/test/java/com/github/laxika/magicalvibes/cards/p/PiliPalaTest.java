package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PiliPalaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} and untapping prompts for a mana color and untaps the source")
    void activatePromptsColorAndUntaps() {
        Permanent pili = addTapped(player1, new PiliPala());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pili.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsOneMana() {
        addTapped(player1, new PiliPala());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(before + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new PiliPala());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    private Permanent addReady(Player player, PiliPala card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, PiliPala card) {
        Permanent perm = addReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
