package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazorBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color grants protection from that color to any permanent you control")
    void choosingColorGrantsProtectionToAnyControlledPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");

        assertThat(land.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing ARTIFACT grants protection from artifacts")
    void choosingArtifactGrantsProtectionFromArtifacts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(creature.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Protection is cleared at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = creature.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        creature.resetModifiers();

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }
}
