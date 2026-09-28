package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManBleedingEdge.class, GrizzlyBears.class})
class IronManBleedingEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an artifact spell as a nonlegendary token only once each turn")
    void copiesArtifactSpellAsNonlegendaryTokenOnlyOnceEachTurn() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.setHand(player1, List.of(legendaryArtifact("Test Artifact"), legendaryArtifact("Second Artifact")));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> copies = findPermanents(player1, "Test Artifact");
        assertThat(copies).hasSize(2);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().getSupertypes())
                        .doesNotContain(CardSupertype.LEGENDARY));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the copy does not use the once-per-turn allowance")
    void decliningCopyDoesNotUseAllowance() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.setHand(player1, List.of(legendaryArtifact("First Artifact"), legendaryArtifact("Second Artifact")));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not trigger for a nonartifact spell")
    void doesNotTriggerForNonartifactSpell() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card legendaryArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return card;
    }
}
