package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cactarantula.class, HostileDesert.class, Shock.class})
class CactarantulaTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast without a Desert for only five mana")
    void cannotCastWithoutDesert() {
        harness.setHand(player1, List.of(new Cactarantula()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs one less to cast while controlling a Desert")
    void costsOneLessWithDesert() {
        harness.addToBattlefield(player1, new HostileDesert());
        harness.setHand(player1, List.of(new Cactarantula()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("May draw when targeted by an opponent's spell")
    void mayDrawWhenTargetedByOpponentSpell() {
        harness.addToBattlefield(player1, new Cactarantula());
        UUID cactarantulaId = harness.getPermanentId(player1, "Cactarantula");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, cactarantulaId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
