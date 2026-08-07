package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SideswipeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the choice moves the Arcane spell's target")
    void retargetsArcaneSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bears1 = harness.getPermanentId(player1, "Grizzly Bears");
        UUID bears2 = harness.getPermanentId(player2, "Grizzly Bears");

        GlacialRay ray = new GlacialRay();
        harness.setHand(player1, List.of(ray));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bears2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ray.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, bears1);

        StackEntry rayEntry = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Glacial Ray"))
                .findFirst().orElseThrow();
        assertThat(rayEntry.getTargetId()).isEqualTo(bears1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the Arcane spell's original target")
    void decliningKeepsOriginalTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bears2 = harness.getPermanentId(player2, "Grizzly Bears");

        GlacialRay ray = new GlacialRay();
        harness.setHand(player1, List.of(ray));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bears2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ray.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-Arcane spell")
    void cannotTargetNonArcaneSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
