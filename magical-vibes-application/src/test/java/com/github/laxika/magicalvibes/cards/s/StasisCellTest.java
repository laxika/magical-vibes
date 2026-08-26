package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({StasisCell.class, GrizzlyBears.class, FountainOfYouth.class})
class StasisCellTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Stasis Cell attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new StasisCell()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stasis Cell")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent aura = new Permanent(new StasisCell());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability moves Stasis Cell to another creature")
    void activatedAbilityMovesAura() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("Activated ability cannot target a noncreature permanent")
    void activatedAbilityCannotTargetNoncreature() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StasisCell());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAura(Player controller, Permanent host) {
        Permanent aura = new Permanent(new StasisCell());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
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

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
