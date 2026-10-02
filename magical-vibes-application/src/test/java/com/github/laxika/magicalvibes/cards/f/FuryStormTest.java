package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuryStorm.class, GrizzlyBears.class, Shock.class})
class FuryStormTest extends BaseCardTest {

    @Test
    void copiesTargetInstantOrSorcery() {
        harness.setLife(player2, 20);
        castShockAndFury();

        resolveWithDeclinedRetargets();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void copiesItselfForEachCommanderCastFromCommandZone() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 4);
        harness.setLife(player2, 20);

        castShockAndFury();

        resolveWithDeclinedRetargets();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new FuryStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an instant or sorcery spell");
    }

    private void castShockAndFury() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new FuryStorm()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, shock.getId());
    }

    private void resolveWithDeclinedRetargets() {
        while (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()) {
            if (gd.interaction.isAwaitingInput()) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                harness.passBothPriorities();
            }
        }
    }
}
