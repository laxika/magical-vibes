package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

class SleightOfMindTest extends BaseCardTest {

    private Permanent paladin(UUID ownerId) {
        return gd.playerBattlefields.get(ownerId).stream()
                .filter(p -> p.getCard().getName().equals("Paladin en-Vec"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Changes a color word on a target permanent")
    void changesColorWordOnTargetPermanent() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(paladin(player2.getId()).getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Only color words may be chosen — a basic land type is rejected")
    void onlyOffersColorWords() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "SWAMP"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A text change to a target spell carries onto the permanent it becomes (CR 613.7)")
    void changesColorWordOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new SleightOfMind(), new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Paladin en-Vec creature spell goes on the stack (index 1; Sleight of Mind stays at index 0).
        harness.castCreature(player1, 1);
        UUID paladinSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, paladinSpellId);
        harness.passBothPriorities(); // resolve Sleight of Mind — begins the color choice

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        harness.passBothPriorities(); // resolve the Paladin en-Vec spell

        assertThat(paladin(player1.getId()).getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Fizzles if the target permanent leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("fizzles"));
    }
}
