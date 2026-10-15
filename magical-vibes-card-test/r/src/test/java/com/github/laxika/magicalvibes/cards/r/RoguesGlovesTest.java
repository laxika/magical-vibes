package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoguesGloves.class, RuneclawBear.class, Forest.class})
class RoguesGlovesTest extends BaseCardTest {

    @Test
    void equipAttachesForTwoMana() {
        Permanent gloves = addGlovesReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gloves.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent gloves = addGlovesReady(player1);
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gloves.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent gloves = addGlovesReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gloves.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedWithOnlyOneMana() {
        Permanent gloves = addGlovesReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gloves.getAttachedTo()).isNull();
    }

    @Test
    void equipmentControllerDrawsWhenOpponentsCreatureDealsDamage() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        creature.setAttacking(true);
        addGlovesReady(player1).setAttachedTo(creature.getId());
        prepareLibraryAndHand();
        harness.setHand(player2, List.of());

        resolveCombat(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void drawTriggerResolvesAfterEquipmentLeavesBattlefield() {
        equipAttackingBears(player1);
        prepareLibraryAndHand();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof RoguesGloves);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting the may draws a card when equipped creature deals combat damage to a player")
    void acceptingDrawsCard() {
        equipAttackingBears(player1);
        prepareLibraryAndHand();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the may draws nothing")
    void decliningDrawsNothing() {
        equipAttackingBears(player1);
        prepareLibraryAndHand();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No trigger when the equipped creature is blocked and deals no damage to a player")
    void noTriggerWhenBlocked() {
        equipAttackingBears(player1);
        prepareLibraryAndHand();

        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        TestCards.mutableCard(blocker).setToughness(10);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when an unequipped creature deals combat damage")
    void noTriggerWhenUnequipped() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        creature.setAttacking(true);
        addGlovesReady(player1);
        prepareLibraryAndHand();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void equipAttackingBears(Player player) {
        Permanent creature = addCreatureReady(player, new RuneclawBear());
        creature.setAttacking(true);
        addGlovesReady(player).setAttachedTo(creature.getId());
    }

    private Permanent addGlovesReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new RoguesGloves());
    }

    private void prepareLibraryAndHand() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
