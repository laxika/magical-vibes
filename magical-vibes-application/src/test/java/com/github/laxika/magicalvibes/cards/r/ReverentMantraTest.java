package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReverentMantraTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain protection from the chosen color")
    void grantsProtectionToAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReverentMantra()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(ownCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(ownOtherCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(opposingCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Can be cast by exiling a white card instead of paying mana")
    void castsWithWhiteCardAlternateCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReverentMantra(), new SuntailHawk()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName()).containsExactly("Suntail Hawk");
    }

    @Test
    @DisplayName("Alternate cost requires a white card")
    void alternateCostRequiresWhiteCard() {
        harness.setHand(player1, List.of(new ReverentMantra(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReverentMantra()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);

        creature.resetModifiers();

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }
}
