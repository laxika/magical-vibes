package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StonehewerGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for an Equipment, puts it onto the battlefield, and attaches to chosen creature")
    void searchesAndAttaches() {
        addReadyGiant(player1);
        Permanent bears = addReadyBears(player1);
        harness.setLibrary(player1, List.of(new StriderHarness()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose the Equipment from the library search
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        // Choose the creature to attach it to
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent equipment = findByName(player1, "Strider Harness");
        assertThat(equipment).isNotNull();
        assertThat(equipment.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can attach the found Equipment to the Giant itself")
    void attachesToGiantItself() {
        Permanent giant = addReadyGiant(player1);
        harness.setLibrary(player1, List.of(new StriderHarness()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.handlePermanentChosen(player1, giant.getId());

        Permanent equipment = findByName(player1, "Strider Harness");
        assertThat(equipment.getAttachedTo()).isEqualTo(giant.getId());
    }

    @Test
    @DisplayName("Finds no Equipment when the library has none")
    void noEquipmentInLibrary() {
        addReadyGiant(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findByName(player1, "Strider Harness")).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyGiant(Player player) {
        Permanent perm = new Permanent(new StonehewerGiant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findByName(Player player, String cardName) {
        for (Permanent p : gd.playerBattlefields.get(player.getId())) {
            if (p.getCard().getName().equals(cardName)) {
                return p;
            }
        }
        return null;
    }
}
