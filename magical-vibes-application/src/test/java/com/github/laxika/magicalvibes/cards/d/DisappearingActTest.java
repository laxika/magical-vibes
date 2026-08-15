package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisappearingActTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent you control as an additional cost and counters target spell")
    void returnsControlledPermanentAndCountersSpell() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent returnedPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisappearingAct()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, spell.getId(), returnedPermanent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Disappearing Act");
    }

    @Test
    @DisplayName("Cannot pay the additional cost with an opponent's permanent")
    void cannotReturnOpponentPermanentAsCost() {
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears spell = new GrizzlyBears();

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisappearingAct()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player2, 0, spell.getId(), opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .contains(opponentPermanent);
    }
}
