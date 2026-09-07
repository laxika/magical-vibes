package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TetsuoUmezawa.class, GrizzlyBears.class, Rancor.class})
class TetsuoUmezawaTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature")
    void destroysTappedCreature() {
        readyTetsuo();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a blocking creature")
    void destroysBlockingCreature() {
        readyTetsuo();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setBlocking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped nonblocking creature")
    void rejectsUntappedNonblockingCreature() {
        readyTetsuo();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aura spells cannot target Tetsuo Umezawa")
    void auraSpellsCannotTargetTetsuoUmezawa() {
        Permanent tetsuo = addCreatureReady(player2, new TetsuoUmezawa());
        harness.setHand(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, tetsuo.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be enchanted by other Auras");
    }

    private void readyTetsuo() {
        addCreatureReady(player1, new TetsuoUmezawa());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
