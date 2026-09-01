package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaponsManufacturing.class, Spellbook.class, GrizzlyBears.class, Shatter.class})
class WeaponsManufacturingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Munitions token when a nontoken artifact enters under your control")
    void createsMunitionsForNontokenArtifact() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Munitions")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create Munitions when a creature enters")
    void doesNotTriggerForCreature() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Munitions")).isEmpty();
    }

    @Test
    @DisplayName("Munitions deals 2 damage to a chosen target when it leaves")
    void munitionsDealsDamageWhenLeaving() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent munitions = findPermanents(player1, "Munitions").getFirst();
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, munitions.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Munitions");
    }
}
