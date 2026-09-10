package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FighterClass.class, Bonesplitter.class, GrizzlyBears.class})
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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

        assertThat(fighterClass.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
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
