package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OathOfMagesTest extends BaseCardTest {

    @Test
    @DisplayName("The active player chooses a higher-life opponent and may deal 1 damage to them")
    void activePlayerMayDealDamageToChosenOpponent() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);
        harness.addToBattlefield(player1, new OathOfMages());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 11);
        harness.addToBattlefield(player1, new OathOfMages());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 11);
    }
}
