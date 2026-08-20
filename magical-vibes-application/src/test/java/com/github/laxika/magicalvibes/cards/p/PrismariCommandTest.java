package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrismariCommandTest extends BaseCardTest {

    @Test
    void dealsDamageAndDestroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(player2.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    void drawsDiscardsAndCreatesTreasureForTheSameTargetPlayer() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Island()));
        prepareSpell();

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void destroyArtifactModeRejectsNonArtifactTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new PrismariCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
