package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrozenSolidTest extends BaseCardTest {

    @Test
    @DisplayName("Frozen Solid attaches to the target creature")
    void attachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new FrozenSolid()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findFrozenSolid(player1);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Frozen Solid prevents the enchanted creature from untapping")
    void preventsUntapping() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();
        Permanent aura = new Permanent(new FrozenSolid());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonlethal damage to the enchanted creature destroys it")
    void damageDestroysEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        Permanent aura = attachFrozenSolid(creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Damage to another creature does not trigger Frozen Solid")
    void damageToAnotherCreatureDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachFrozenSolid(enchanted);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, other.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(other);
    }

    @Test
    @DisplayName("Frozen Solid can target only a creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent land = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new FrozenSolid()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachFrozenSolid(Permanent creature) {
        Permanent aura = new Permanent(new FrozenSolid());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private Permanent findFrozenSolid(Player controller) {
        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FrozenSolid)
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextTurn(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
