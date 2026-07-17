package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagicalHackTest extends BaseCardTest {

    private Permanent grizzly(UUID ownerId) {
        return gd.playerBattlefields.get(ownerId).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Changes a basic land type on a target permanent")
    void changesLandTypeOnTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(grizzly(player2.getId()).getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Plains"));
    }

    @Test
    @DisplayName("Only basic land types may be chosen — a color word is rejected")
    void onlyOffersLandTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLACK"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A land-type change to a target spell carries onto the permanent it becomes (CR 613.7)")
    void changesLandTypeOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new MagicalHack(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Grizzly Bears creature spell goes on the stack (index 1; Magical Hack stays at index 0).
        harness.castCreature(player1, 1);
        UUID grizzlySpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, grizzlySpellId);
        harness.passBothPriorities(); // resolve Magical Hack — begins the land-type choice

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        harness.passBothPriorities(); // resolve the Grizzly Bears spell

        assertThat(grizzly(player1.getId()).getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Plains"));
    }
}
