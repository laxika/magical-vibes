package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalEddy.class, Forest.class, GrizzlyBears.class, Pacifism.class})
class TemporalEddyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target creature on top of its owner's library")
    void putsCreatureOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castAndResolve(targetId);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Puts a target land on top of its owner's library")
    void putsLandOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castAndResolve(targetId);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).extracting(Card::getName)
                .isEqualTo("Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a noncreature nonland permanent")
    void cannotTargetNonCreatureNonlandPermanent() {
        harness.addToBattlefield(player2, new Pacifism());
        UUID targetId = harness.getPermanentId(player2, "Pacifism");

        harness.setHand(player1, List.of(new TemporalEddy()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or land");
    }

    private void castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new TemporalEddy()));
        addMana();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
