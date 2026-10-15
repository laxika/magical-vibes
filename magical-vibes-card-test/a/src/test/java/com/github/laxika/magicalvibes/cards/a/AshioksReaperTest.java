package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
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

@CardUsed({AshioksReaper.class, GloriousAnthem.class, Naturalize.class, CandyGrapple.class})
class AshioksReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an enchantment you control is put into a graveyard")
    void drawsWhenControlledEnchantmentIsPutIntoGraveyard() {
        harness.addToBattlefield(player1, new AshioksReaper());
        UUID anthemId = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem()).getId();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, anthemId);

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
        UUID anthemId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, anthemId);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Reaper draws separately for the same enchantment")
    void eachReaperDrawsForTheSameEnchantment() {
        harness.addToBattlefield(player1, new AshioksReaper());
        harness.addToBattlefield(player1, new AshioksReaper());
        UUID anthemId = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem()).getId();
        harness.setLibrary(player1, List.of(new AshioksReaper(), new AshioksReaper()));
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, anthemId);

        assertThat(gd.stack).hasSize(2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws when an enchantment is sacrificed to bargain")
    void drawsWhenEnchantmentIsSacrificedAsACastingCost() {
        UUID reaperId = harness.addToBattlefieldAndReturn(player1, new AshioksReaper()).getId();
        UUID anthemId = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem()).getId();
        harness.setLibrary(player1, List.of(new AshioksReaper()));
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, reaperId, anthemId);

        harness.assertInGraveyard(player1, "Glorious Anthem");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Ashiok's Reaper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending draw still resolves after the Reaper dies")
    void pendingDrawResolvesAfterReaperDies() {
        UUID reaperId = harness.addToBattlefieldAndReturn(player1, new AshioksReaper()).getId();
        UUID anthemId = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem()).getId();
        harness.setLibrary(player1, List.of(new AshioksReaper()));
        harness.setHand(player1, List.of(new Naturalize(), new CandyGrapple()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, anthemId);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, reaperId);

        harness.assertInGraveyard(player1, "Ashiok's Reaper");
        assertThat(gd.stack).hasSize(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).isEmpty();
    }
}
