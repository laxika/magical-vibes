package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseOfSilence.class, GrizzlyBears.class, Shock.class})
class CurseOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Curse of Silence attaches it to the target player and chooses a name")
    void resolvingAttachesAndChoosesName() {
        castCurseOfSilence();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Grizzly Bears");

        Permanent curse = findPermanent(player1, "Curse of Silence");
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(curse.getChosenName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("The enchanted player pays {2} more for spells with the chosen name")
    void taxesChosenNameForEnchantedPlayer() {
        castCurseOfSilence();
        harness.handleListChoice(player1, "Grizzly Bears");

        preparePlayer2MainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A different player and a different name are not affected by the tax")
    void taxOnlyAppliesToEnchantedPlayerAndChosenName() {
        castCurseOfSilence();
        harness.handleListChoice(player1, "Grizzly Bears");

        preparePlayer2MainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        preparePlayer1MainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("When the enchanted player casts the chosen spell, sacrificing the Curse draws a card")
    void maySacrificeToDraw() {
        castCurseOfSilence();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.setLibrary(player1, List.of(new Shock()));

        preparePlayer2MainPhase();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.castCreature(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Curse of Silence");
        harness.assertInGraveyard(player1, "Curse of Silence");
        harness.assertInHand(player1, "Shock");
    }

    private void castCurseOfSilence() {
        harness.setHand(player1, List.of(new CurseOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void preparePlayer2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void preparePlayer1MainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
