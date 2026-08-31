package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshioksReaper.class, GloriousAnthem.class, Naturalize.class})
class AshioksReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an enchantment you control is put into a graveyard")
    void drawsWhenControlledEnchantmentIsPutIntoGraveyard() {
        harness.addToBattlefield(player1, new AshioksReaper());
        harness.addToBattlefield(player1, new GloriousAnthem());
        UUID anthemId = harness.getPermanentId(player1, "Glorious Anthem");

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, anthemId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for an enchantment an opponent controls")
    void doesNotTriggerForOpponentControlledEnchantment() {
        harness.addToBattlefield(player1, new AshioksReaper());
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID anthemId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, anthemId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
