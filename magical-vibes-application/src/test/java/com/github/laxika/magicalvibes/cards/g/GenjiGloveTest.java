package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.u.UndercityDireRat;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GenjiGlove.class, UndercityDireRat.class, Humble.class})
class GenjiGloveTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has double strike")
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new UndercityDireRat());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking in the first combat phase untaps the creature and grants another combat phase")
    void firstCombatAttackUntapsAndGrantsExtraCombat() {
        Permanent creature = addCreatureReady(player1, new UndercityDireRat());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), 1);
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking in a later combat phase does not untap or grant another combat phase")
    void laterCombatAttackDoesNothing() {
        Permanent creature = addCreatureReady(player1, new UndercityDireRat());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), 2);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches for three mana and grants double strike")
    void equipAttachesToControlledCreature() {
        Permanent creature = addCreatureReady(player1, new UndercityDireRat());
        Permanent glove = addGloveReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(glove.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Removing the equipped creature's abilities does not remove the Equipment's attack trigger")
    void creatureLosingAbilitiesDoesNotPreventGloveTrigger() {
        Permanent creature = addCreatureReady(player1, new UndercityDireRat());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();

        declareAttackers(player1, List.of(0), 1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("The Equipment's controller controls the attack trigger even when an opponent controls its creature")
    void equipmentControllerControlsTrigger() {
        Permanent creature = addCreatureReady(player1, new UndercityDireRat());
        Permanent glove = addGloveReady(player2);
        glove.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(glove.getId());
    }

    @Test
    @DisplayName("Removing the Equipment after it triggers does not stop untapping or the extra combat")
    void triggerResolvesAfterEquipmentLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new UndercityDireRat());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());
        declareAttackers(player1, List.of(0), 1);
        gd.playerBattlefields.get(player1.getId()).remove(glove);

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    private Permanent addGloveReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GenjiGlove());
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player, attackerIndices));
    }
}
