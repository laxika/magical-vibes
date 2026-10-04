package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.d.DuelingRapier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({FighterClass.class, Bonesplitter.class, DuelingRapier.class, GrizzlyBears.class})
class FighterClassTest extends BaseCardTest {

    @Test
    void entersAndSearchesForEquipment() {
        GrizzlyBears bears = new GrizzlyBears();
        Bonesplitter equipment = new Bonesplitter();
        harness.setHand(player1, List.of(new FighterClass()));
        harness.setLibrary(player1, List.of(bears, equipment));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(equipment);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void levelTwoReducesEquipCosts() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        levelUpToTwo(fighterClass);

        prepareForSorcery();
        harness.activateAbility(player1, battlefieldIndex(equipment), null, creature.getId());
        harness.passBothPriorities();

        assertThat(fighterClass.getClassLevel()).isEqualTo(2);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void levelThreeCanRequireCreatureToBlockAttacker() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        levelUpToThree(fighterClass);

        declareAttackers(List.of(battlefieldIndex(attacker)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(blocker.getId());

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                battlefieldIndex(blocker, player2), battlefieldIndex(attacker))));
    }

    @Test
    void levelThreeMayDeclineTargeting() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        levelUpToThree(fighterClass);

        declareAttackers(List.of(battlefieldIndex(attacker)));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).doesNotContain(attacker.getId());
    }

    @Test
    void levelOneDoesNotTriggerWhenCreatureAttacks() {
        harness.addToBattlefield(player1, new FighterClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(battlefieldIndex(attacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelTwoDoesNotTriggerWhenCreatureAttacks() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        levelUpToTwo(fighterClass);

        declareAttackers(List.of(battlefieldIndex(attacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelThreeRetainsEquipReduction() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DuelingRapier());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        levelUpToThree(fighterClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(equipment), null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipmentSearchMayFailToFind() {
        Bonesplitter equipment = new Bonesplitter();
        harness.setHand(player1, List.of(new FighterClass()));
        harness.setLibrary(player1, List.of(equipment));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
    }

    @Test
    void levelOneDoesNotReduceEquipCost() {
        harness.addToBattlefield(player1, new FighterClass());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        prepareForSorcery();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(equipment), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void multipleLevelTwoClassesStackTheirEquipReduction() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DuelingRapier());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        levelUpToTwo(first);
        levelUpToTwo(second);
        prepareForSorcery();

        harness.activateAbility(player1, battlefieldIndex(equipment), null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void cannotSkipLevelTwo() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(fighterClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fighterClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void cannotGainLevelAtInstantSpeed() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(fighterClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fighterClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void tappedTargetDoesNotHaveToBlock() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();
        levelUpToThree(fighterClass);
        declareAttackers(List.of(battlefieldIndex(attacker)));
        harness.handlePermanentChosen(player1, blocker.getId());
        resolveAllTriggers();

        prepareDeclareBlockers();
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    void blockingRequirementExpiresBeforeAnotherCombat() {
        Permanent fighterClass = harness.addToBattlefieldAndReturn(player1, new FighterClass());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        levelUpToThree(fighterClass);
        declareAttackers(List.of(battlefieldIndex(attacker)));
        harness.handlePermanentChosen(player1, blocker.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        attacker.untap();
        declareAttackers(List.of(battlefieldIndex(attacker)));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        prepareDeclareBlockers();
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
    }

    private void levelUpToTwo(Permanent fighterClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, battlefieldIndex(fighterClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent fighterClass) {
        levelUpToTwo(fighterClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, battlefieldIndex(fighterClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return battlefieldIndex(permanent, player1);
    }

    private int battlefieldIndex(Permanent permanent, com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
