package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RadiantsDragoonsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 5 life")
    void entryGainsFiveLife() {
        castAndResolveDragoons();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Paying echo keeps Radiant's Dragoons and echo does not trigger again")
    void payingEchoKeepsDragoonsAndIsOneShot() {
        castAndResolveDragoons();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Radiant's Dragoons");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Radiant's Dragoons");
    }

    @Test
    @DisplayName("Declining echo sacrifices Radiant's Dragoons")
    void decliningEchoSacrificesDragoons() {
        castAndResolveDragoons();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Radiant's Dragoons");
        harness.assertInGraveyard(player1, "Radiant's Dragoons");
    }

    private void castAndResolveDragoons() {
        harness.setHand(player1, List.of(new RadiantsDragoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Radiant's Dragoons");
    }
}
