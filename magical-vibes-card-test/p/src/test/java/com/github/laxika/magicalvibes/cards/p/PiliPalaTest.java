package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiliPala.class})
class PiliPalaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} and untapping prompts for a mana color and untaps the source")
    void activatePromptsColorAndUntaps() {
        Permanent pili = addTapped(player1, new PiliPala());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pili.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
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
        addCreatureReady(player1, new PiliPala());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The mana ability resolves immediately and can produce each color")
    void producesEachColorWithoutUsingStack(ManaColor color) {
        Permanent pili = addTapped(player1, new PiliPala());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(pili.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
    }

    @Test
    @DisplayName("A tapped summoning-sick Pili-Pala cannot pay the untap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent pili = harness.addToBattlefieldAndReturn(player1, new PiliPala());
        pili.setSummoningSick(true);
        pili.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(pili.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Insufficient mana prevents activation without untapping the source")
    void cannotActivateWithoutTwoMana() {
        Permanent pili = addTapped(player1, new PiliPala());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(pili.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addTapped(Player player, PiliPala card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
