package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaleWayfarerTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{W}{W}, {Q}: target creature gains protection from the chosen color and the source untaps")
    void grantsProtectionAndUntaps() {
        Permanent wayfarer = addTapped(player1, new PaleWayfarer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null).isTrue();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId)).findFirst().orElseThrow();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        // Paying {Q} untapped the source.
        assertThat(wayfarer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When targeting an opponent's creature, that creature's controller chooses the color")
    void targetControllerChoosesColor() {
        addTapped(player1, new PaleWayfarer());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        // The choice belongs to the target's controller (player2), not the ability's controller.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null).isTrue();
        harness.handleListChoice(player2, "GREEN");

        assertThat(opponentCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addReady(player1, new PaleWayfarer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        addTapped(player1, new PaleWayfarer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId)).findFirst().orElseThrow();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, Card card) {
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
