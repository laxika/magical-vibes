package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CopperGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and puts an artifact card from hand onto the battlefield")
    void putsArtifactFromHandOntoBattlefield() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Copper Gnomes");
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Offers only artifact cards from hand")
    void offersOnlyArtifactCards() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Declining leaves the artifact card in hand after sacrificing itself")
    void decliningLeavesArtifactInHand() {
        harness.addToBattlefield(player1, new CopperGnomes());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        activate();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Copper Gnomes");
        harness.assertInHand(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    private void activate() {
        harness.activateAbility(player1, 0, null, null);
    }
}
