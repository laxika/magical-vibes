package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepelTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Repel puts target creature on top of its owner's library")
    void resolvingPutsTargetCreatureOnTopOfOwnersLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = creature.getId();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        Repel repel = new Repel();
        harness.setHand(player1, List.of(repel));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(creature.getCard());
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .first()
                .isSameAs(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(repel);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        UUID targetId = land.getId();

        harness.setHand(player1, List.of(new Repel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    @DisplayName("Repel fizzles if the target is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = creature.getId();
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        Repel repel = new Repel();
        harness.setHand(player1, List.of(repel));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(repel);
    }
}
