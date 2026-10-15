package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighFaeTrickster.class, GrizzlyBears.class, LavaAxe.class})
class HighFaeTricksterTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a creature spell at instant speed")
    void canCastCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new HighFaeTrickster());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast a sorcery spell at instant speed")
    void canCastSorceryAtInstantSpeed() {
        harness.addToBattlefield(player1, new HighFaeTrickster());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lava Axe");
    }

    @Test
    @DisplayName("Only its controller's spells gain flash")
    void onlyAffectsController() {
        harness.addToBattlefield(player1, new HighFaeTrickster());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells lose flash when High Fae Trickster leaves the battlefield")
    void losesFlashWhenItLeaves() {
        harness.addToBattlefield(player1, new HighFaeTrickster());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("High Fae Trickster itself can be cast during an opponent's turn")
    void canCastTricksterDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new HighFaeTrickster(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "High Fae Trickster");
    }

    @Test
    @DisplayName("Can cast a creature in response to an opponent's spell")
    void canRespondToOpponentsSpell() {
        harness.addToBattlefield(player1, new HighFaeTrickster());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LavaAxe()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castSorcery(player2, 0, player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isInstanceOf(GrizzlyBears.class);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("A Trickster in hand does not grant flash to other spells")
    void tricksterInHandDoesNotGrantFlash() {
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new HighFaeTrickster(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
