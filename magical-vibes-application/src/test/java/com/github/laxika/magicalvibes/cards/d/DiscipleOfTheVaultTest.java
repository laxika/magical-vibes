package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscipleOfTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger makes a target opponent lose 1 life")
    void acceptingTriggerMakesTargetOpponentLoseLife() {
        harness.addToBattlefield(player1, new DiscipleOfTheVault());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);

        destroyOpponentArtifact();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Declining the trigger does not make an opponent lose life")
    void decliningTriggerDoesNotMakeOpponentLoseLife() {
        harness.addToBattlefield(player1, new DiscipleOfTheVault());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLife(player2, 20);

        destroyOpponentArtifact();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    private void destroyOpponentArtifact() {
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Mind Stone"));
        harness.passBothPriorities();
    }
}
