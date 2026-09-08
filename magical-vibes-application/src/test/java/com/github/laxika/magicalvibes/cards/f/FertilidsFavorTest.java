package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FertilidsFavor.class, AetherSpellbomb.class, Forest.class, GrizzlyBears.class,
        Island.class, Mountain.class})
class FertilidsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gets a basic land tapped and the target artifact gets two counters")
    void searchesTargetPlayersLibraryAndCountersArtifact() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.setHand(player1, List.of(new FertilidsFavor()));
        harness.setLibrary(player2, List.of(new Forest(), new Island(), new Mountain(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID artifactId = harness.getPermanentId(player1, "Aether Spellbomb");
        harness.castInstant(player1, 0, List.of(player2.getId(), artifactId));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND)
                && card.getSupertypes().contains(CardSupertype.BASIC));

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Aether Spellbomb"))
                .findFirst().orElseThrow();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().hasType(CardType.LAND)
                        && permanent.isTapped());
    }

    @Test
    @DisplayName("The counter target is optional")
    void mayOmitCounterTarget() {
        harness.setHand(player1, List.of(new FertilidsFavor()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
    }

    @Test
    @DisplayName("Only artifacts and creatures can receive the counters")
    void rejectsLandAsCounterTarget() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new FertilidsFavor()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), landId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }
}
