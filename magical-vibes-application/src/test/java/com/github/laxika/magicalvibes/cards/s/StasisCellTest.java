package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
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

@CardUsed({StasisCell.class, BorosRecruit.class, BorosSignet.class})
class StasisCellTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Stasis Cell attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new BorosRecruit());

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
        Permanent creature = addCreatureReady(player2, new BorosRecruit());
        creature.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StasisCell());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability moves Stasis Cell to another creature")
    void activatedAbilityMovesAura() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StasisCell());
        aura.setAttachedTo(first.getId());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Attaching Stasis Cell does not tap an untapped creature")
    void attachingDoesNotTapCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StasisCell());
        aura.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Moving Stasis Cell releases the old creature and locks the new creature")
    void movingAuraTransfersUntapPrevention() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        first.tap();
        second.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StasisCell());
        aura.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
    }
    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
