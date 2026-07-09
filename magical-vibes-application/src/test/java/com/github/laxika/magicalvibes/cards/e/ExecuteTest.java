package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecuteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Execute destroys a white creature and draws a card")
    void resolvingDestroysWhiteCreatureAndDraws() {
        Permanent hawk = new Permanent(new EliteVanguard());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(hawk);

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, hawk.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        // Execute was cast from a one-card hand, so the only card in hand is the one drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Execute destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent hawk = new Permanent(new EliteVanguard());
        hawk.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(hawk);

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, hawk.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        // A legal white target elsewhere keeps Execute playable, so the rejection is the filter message.
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new EliteVanguard()));

        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }
}
