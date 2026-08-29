package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class WithdrawTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the first creature and returns the second when its controller declines")
    void returnsBothCreaturesWhenSecondControllerDeclines() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        castWithdraw(first, second);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns the first creature and keeps the second when its controller pays")
    void keepsSecondCreatureWhenItsControllerPays() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        castWithdraw(first, second);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void cannotChooseSameCreatureTwice() {
        Permanent creature = addCreature(player2);
        harness.setHand(player1, List.of(new Withdraw()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castWithdraw(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new Withdraw()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID firstId = first.getId();
        UUID secondId = second.getId();
        harness.castInstant(player1, 0, List.of(firstId, secondId));
    }
}
