package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VedalkenOrreryTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a creature at instant speed with Vedalken Orrery")
    void canCastCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can cast a sorcery at instant speed with Vedalken Orrery")
    void canCastSorceryAtInstantSpeed() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Vedalken Orrery only grants flash to its controller")
    void onlyAffectsController() {
        harness.addToBattlefield(player2, new VedalkenOrrery());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells lose flash timing when Vedalken Orrery leaves the battlefield")
    void losesFlashWhenOrreryLeaves() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
