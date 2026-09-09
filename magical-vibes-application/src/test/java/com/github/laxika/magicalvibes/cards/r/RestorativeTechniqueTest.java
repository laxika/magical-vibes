package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({RestorativeTechnique.class, Forest.class, GrizzlyBears.class, Island.class})
class RestorativeTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains life, gets a basic land tapped, and the creature gets a counter")
    void resolvesAllEffects() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RestorativeTechnique()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(player2.getId(), bearId));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND)
                && card.getSupertypes().contains(CardSupertype.BASIC));

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
    }

    @Test
    @DisplayName("The creature target is optional")
    void mayOmitCreatureTarget() {
        harness.setHand(player1, List.of(new RestorativeTechnique()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
    }

    @Test
    @DisplayName("Only a creature can be the optional counter target")
    void rejectsNonCreatureCounterTarget() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new RestorativeTechnique()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID islandId = harness.getPermanentId(player1, "Island");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId(), islandId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
