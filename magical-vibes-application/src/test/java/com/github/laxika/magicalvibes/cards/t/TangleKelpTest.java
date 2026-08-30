package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleKelp.class, GrizzlyBears.class, Plains.class})
class TangleKelpTest extends BaseCardTest {

    @Test
    @DisplayName("Tangle Kelp taps the enchanted creature when it enters")
    void tapsEnchantedCreatureOnEnter() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TangleKelp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangle Kelp prevents a creature that attacked last turn from untapping")
    void preventsUntapAfterCreatureAttackedLastTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        creature.setAttackedDuringControllersCurrentTurn(true);
        attachTangleKelp(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangle Kelp does not prevent untapping when the creature did not attack last turn")
    void allowsUntapAfterCreatureDidNotAttackLastTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachTangleKelp(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tangle Kelp cannot enchant a land")
    void cannotEnchantLand() {
        harness.addToBattlefield(player2, new Plains());
        Permanent land = findPermanent(player2, "Plains");

        harness.setHand(player1, List.of(new TangleKelp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachTangleKelp(Permanent creature) {
        Permanent aura = new Permanent(new TangleKelp());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
