package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AetherSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the blue ability returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = findPermanent(player2, "Grizzly Bears").getId();
        harness.activateAbility(player1, 0, null, targetId);

        harness.assertNotOnBattlefield(player1, "Aether Spellbomb");
        harness.assertInGraveyard(player1, "Aether Spellbomb");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving the colorless ability draws a card")
    void drawsACard() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aether Spellbomb");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The blue ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = findPermanent(player2, "Island").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        harness.assertOnBattlefield(player1, "Aether Spellbomb");
    }
}
