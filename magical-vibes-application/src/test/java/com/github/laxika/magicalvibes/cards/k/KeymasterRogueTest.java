package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeymasterRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Keymaster Rogue cannot be blocked")
    void cannotBeBlocked() {
        Permanent keymaster = addCreatureReady(player1, new KeymasterRogue());

        assertThat(keymaster.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Entering prompts for a creature you control and returns the choice to its owner's hand")
    void entersAndReturnsChosenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KeymasterRogue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(opponentBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Entering must return Keymaster Rogue when it is the only creature you control")
    void returnsItselfWhenItIsTheOnlyCreature() {
        harness.setHand(player1, List.of(new KeymasterRogue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent keymaster = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof KeymasterRogue)
                .findFirst()
                .orElseThrow();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(keymaster.getId());

        harness.handlePermanentChosen(player1, keymaster.getId());

        harness.assertNotOnBattlefield(player1, "Keymaster Rogue");
        harness.assertInHand(player1, "Keymaster Rogue");
    }
}
