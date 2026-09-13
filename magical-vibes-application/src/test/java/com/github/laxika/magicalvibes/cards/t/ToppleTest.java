package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BelbesPortal;
import com.github.laxika.magicalvibes.cards.f.FlowstoneCrusher;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Topple.class, BelbesPortal.class, FlowstoneCrusher.class, Mossdog.class})
class ToppleTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the creature with the greatest power on the battlefield")
    void exilesGreatestPowerCreature() {
        harness.addToBattlefield(player1, new Mossdog());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());

        castTopple(target);

        harness.assertNotOnBattlefield(player2, "Flowstone Crusher");
        harness.assertOnBattlefield(player1, "Mossdog");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Flowstone Crusher"));
    }

    @Test
    @DisplayName("Allows choosing any creature tied for greatest power")
    void allowsTiedGreatestPowerCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());
        harness.addToBattlefield(player1, new Mossdog());
        harness.addToBattlefield(player2, new FlowstoneCrusher());

        castTopple(first);

        harness.assertOnBattlefield(player2, "Flowstone Crusher");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Flowstone Crusher"));
    }

    @Test
    @DisplayName("Rejects targeting a creature that is not tied for greatest power")
    void rejectsLowerPowerTarget() {
        Permanent lower = harness.addToBattlefieldAndReturn(player2, new Mossdog());
        harness.addToBattlefield(player2, new FlowstoneCrusher());
        prepareTopple();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, lower.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects targeting a noncreature permanent")
    void rejectsNonCreaturePermanentTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BelbesPortal());
        harness.addToBattlefield(player1, new FlowstoneCrusher());
        prepareTopple();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTopple(Permanent target) {
        prepareTopple();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareTopple() {
        harness.setHand(player1, List.of(new Topple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
