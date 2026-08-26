package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverTheEdge.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class OverTheEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        cast(0, artifact.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Target creature you control explores twice")
    void targetCreatureExploresTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();

        harness.castSorcery(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstLand.getId(), secondLand.getId());
    }

    @Test
    @DisplayName("Destroy mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Explore mode rejects a creature controlled by an opponent")
    void exploreModeRejectsOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();
        harness.castSorcery(player1, 0, mode, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
