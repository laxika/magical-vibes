package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PadeemConsulOfInnovationTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts you control have hexproof, but opponents' artifacts do not")
    void grantsHexproofToControlledArtifactsOnly() {
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent ownArtifact = findPermanent(player1, "Spellbook");
        Permanent opponentArtifact = findPermanent(player2, "Spellbook");

        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Draws a card when your artifact is tied for the highest mana value")
    void drawsWhenArtifactIsTiedForHighestManaValue() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.addToBattlefield(player2, new ObeliskOfBant());

        advanceToAndResolveUpkeep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent controls a higher-mana-value artifact")
    void doesNotDrawWhenOpponentControlsHigherManaValueArtifact() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new ObeliskOfBant());

        advanceToAndResolveUpkeep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToAndResolveUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
