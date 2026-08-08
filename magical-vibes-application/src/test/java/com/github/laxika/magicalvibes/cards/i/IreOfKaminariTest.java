package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IreOfKaminariTest extends BaseCardTest {

    private void giveCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Deals damage to any target equal to Arcane cards in controller's graveyard")
    void dealsDamageEqualToArcaneCardsInGraveyard() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new GlacialRay(), new GlacialRay(), new GlacialRay()));
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals zero damage when no Arcane cards in graveyard")
    void dealsZeroDamageWithNoArcane() {
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not count non-Arcane cards in graveyard")
    void doesNotCountNonArcaneCards() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new LlanowarElves(), new GlacialRay()));
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not count itself while resolving — still on the stack")
    void doesNotCountItselfWhileResolving() {
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Ire of Kaminari");
    }

    @Test
    @DisplayName("Can target a creature")
    void dealsDamageToTargetCreature() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new GlacialRay(), new GlacialRay()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
