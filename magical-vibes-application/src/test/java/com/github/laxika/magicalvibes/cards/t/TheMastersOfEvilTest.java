package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ConstructACosmicCube;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MadameMasque;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMastersOfEvil.class, ConstructACosmicCube.class, GrizzlyBears.class, MadameMasque.class})
class TheMastersOfEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Other Villains you control get +2/+1")
    void buffsOtherVillainsYouControl() {
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new MadameMasque());
        int basePower = gqs.getEffectivePower(gd, villain);
        int baseToughness = gqs.getEffectiveToughness(gd, villain);

        harness.addToBattlefield(player1, new TheMastersOfEvil());

        assertThat(gqs.getEffectivePower(gd, villain)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, villain)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Does not buff itself, non-Villains, or an opponent's Villains")
    void doesNotBuffExcludedCreatures() {
        TheMastersOfEvil card = new TheMastersOfEvil();
        card.setPower(5);
        card.setToughness(6);
        Permanent masters = harness.addToBattlefieldAndReturn(player1, card);
        Permanent nonVillain = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVillain = harness.addToBattlefieldAndReturn(player2, new MadameMasque());
        int nonVillainPower = gqs.getEffectivePower(gd, nonVillain);
        int nonVillainToughness = gqs.getEffectiveToughness(gd, nonVillain);
        int opponentVillainPower = gqs.getEffectivePower(gd, opponentVillain);
        int opponentVillainToughness = gqs.getEffectiveToughness(gd, opponentVillain);

        assertThat(gqs.getEffectivePower(gd, masters)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, masters)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonVillain)).isEqualTo(nonVillainPower);
        assertThat(gqs.getEffectiveToughness(gd, nonVillain)).isEqualTo(nonVillainToughness);
        assertThat(gqs.getEffectivePower(gd, opponentVillain)).isEqualTo(opponentVillainPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentVillain)).isEqualTo(opponentVillainToughness);
    }

    @Test
    @DisplayName("Can be discarded from hand to search for a Plan card")
    void searchesForPlanFromHand() {
        TheMastersOfEvil source = new TheMastersOfEvil();
        ConstructACosmicCube plan = new ConstructACosmicCube();
        harness.setHand(player1, List.of(source));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), plan));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Masters of Evil");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof ConstructACosmicCube)
                .hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Construct a Cosmic Cube");
    }
}
