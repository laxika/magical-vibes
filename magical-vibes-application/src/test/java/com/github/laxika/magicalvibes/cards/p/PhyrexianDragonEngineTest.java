package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianDragonEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Casting it from hand does not trigger its graveyard-entered ability")
    void castingFromHandDoesNotTriggerGraveyardAbility() {
        harness.setHand(player1, List.of(new PhyrexianDragonEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Phyrexian Dragon Engine");
    }

    @Test
    @DisplayName("Unearth lets you discard your hand and draw three cards")
    void unearthMayDiscardHandAndDrawThree() {
        prepareUnearth();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        harness.activateGraveyardAbility(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining the graveyard-entered ability keeps your hand")
    void decliningGraveyardAbilityKeepsHand() {
        prepareUnearth();
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        harness.activateGraveyardAbility(player1, 0);
        resolveUntilInputOrEmpty();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void prepareUnearth() {
        harness.setGraveyard(player1, List.of(new PhyrexianDragonEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 10 && !gd.interaction.isAwaitingInput() && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
