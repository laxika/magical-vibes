package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VengevineTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard on the second creature spell, ignoring noncreature spells")
    void returnsOnSecondCreatureSpell() {
        Vengevine vengevine = new Vengevine();
        harness.setGraveyard(player1, List.of(vengevine));
        harness.setHand(player1, List.of(new Spellbook(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Vengevine");
        harness.assertNotInGraveyard(player1, "Vengevine");
    }

    @Test
    @DisplayName("The first creature spell does not trigger Vengevine")
    void firstCreatureSpellDoesNotTrigger() {
        Vengevine vengevine = new Vengevine();
        harness.setGraveyard(player1, List.of(vengevine));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vengevine");
    }

    @Test
    @DisplayName("Declining the trigger keeps Vengevine in the graveyard")
    void declineKeepsVengevineInGraveyard() {
        Vengevine vengevine = new Vengevine();
        harness.setGraveyard(player1, List.of(vengevine));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Vengevine");
    }
}
