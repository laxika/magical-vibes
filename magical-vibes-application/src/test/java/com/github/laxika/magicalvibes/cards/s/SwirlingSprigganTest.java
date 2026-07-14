package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwirlingSprigganTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the controller for a color choice")
    void resolvingPromptsColorChoice() {
        Permanent spriggan = addReadySpriggan(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        activate(player1, spriggan, bears);
        harness.passBothPriorities(); // resolve the ability

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a single color makes the target only that color until end of turn")
    void singleColorReplacesColors() {
        Permanent bears = resolveAndChoose(player1, "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing several colors makes the target all of those colors")
    void multipleColorsReplaceColors() {
        Permanent bears = resolveAndChoose(player1, "WHITE", "BLUE", "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = resolveAndChoose(player1, "BLUE", "DONE"); // GrizzlyBears is green
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);

        bears.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void rejectsCreatureYouDoNotControl() {
        Permanent spriggan = addReadySpriggan(player1);
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> activate(player1, spriggan, enemyBears))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent resolveAndChoose(Player controller, String... choices) {
        Permanent spriggan = addReadySpriggan(controller);
        Permanent bears = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        harness.addMana(controller, ManaColor.GREEN, 2);

        activate(controller, spriggan, bears);
        harness.passBothPriorities(); // resolve the ability -> begins the color choice
        for (String choice : choices) {
            harness.handleListChoice(controller, choice);
        }
        return bears;
    }

    private void activate(Player controller, Permanent spriggan, Permanent target) {
        int index = gd.playerBattlefields.get(controller.getId()).indexOf(spriggan);
        harness.activateAbility(controller, index, 0, null, target.getId());
    }

    private Permanent addReadySpriggan(Player player) {
        Permanent perm = new Permanent(new SwirlingSpriggan());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
