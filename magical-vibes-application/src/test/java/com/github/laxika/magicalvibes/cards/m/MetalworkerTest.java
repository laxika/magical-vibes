package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetalworkerTest extends BaseCardTest {

    @Test
    @DisplayName("Produces two colorless mana for each revealed artifact card")
    void producesManaForRevealedArtifacts() {
        addReadyMetalworker();
        FountainOfYouth fountain = new FountainOfYouth();
        Spellbook spellbook = new Spellbook();
        GiantGrowth nonArtifact = new GiantGrowth();
        harness.setHand(player1, List.of(fountain, spellbook, nonArtifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(fountain.getId(), spellbook.getId());

        harness.handleMultipleCardsChosen(player1, List.of(fountain.getId(), spellbook.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    @DisplayName("Allows revealing zero artifact cards")
    void producesNoManaForZeroRevealedArtifacts() {
        addReadyMetalworker();
        GiantGrowth nonArtifact = new GiantGrowth();
        harness.setHand(player1, List.of(nonArtifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void addReadyMetalworker() {
        Permanent metalworker = harness.addToBattlefieldAndReturn(player1, new Metalworker());
        metalworker.setSummoningSick(false);
    }
}
