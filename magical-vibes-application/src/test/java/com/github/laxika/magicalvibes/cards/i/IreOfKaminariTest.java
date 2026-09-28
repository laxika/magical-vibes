package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IreOfKaminari.class, FirstVolley.class, BileUrchin.class})
class IreOfKaminariTest extends BaseCardTest {

    private void giveCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Deals damage to any target equal to Arcane cards in controller's graveyard")
    void dealsDamageEqualToArcaneCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new FirstVolley(), new FirstVolley(), new FirstVolley()));
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals zero damage when no Arcane cards in graveyard")
    void dealsZeroDamageWithNoArcane() {
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not count non-Arcane cards in graveyard")
    void doesNotCountNonArcaneCards() {
        harness.setGraveyard(player1, List.of(new BileUrchin(), new FirstVolley()));
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not count itself while resolving — still on the stack")
    void doesNotCountItselfWhileResolving() {
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Ire of Kaminari");
    }

    @Test
    @DisplayName("Can target a creature")
    void dealsDamageToTargetCreature() {
        harness.setGraveyard(player1, List.of(new FirstVolley(), new FirstVolley()));
        harness.addToBattlefield(player2, new BileUrchin());
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        UUID urchinId = harness.getPermanentId(player2, "Bile Urchin");
        harness.castAndResolveInstant(player1, 0, urchinId);

        harness.assertNotOnBattlefield(player2, "Bile Urchin");
        harness.assertInGraveyard(player2, "Bile Urchin");
    }

    @Test
    @DisplayName("Counts only Arcane cards in the spell controller's graveyard")
    void doesNotCountOpponentsArcaneCards() {
        harness.setGraveyard(player2, List.of(new FirstVolley()));
        harness.setHand(player1, List.of(new IreOfKaminari()));
        giveCastingMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
