package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeirloomEpic.class, GrizzlyBears.class, Forest.class, BarkformHarvester.class})
class HeirloomEpicTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping four creatures can pay the entire activation cost")
    void tapsCreaturesInsteadOfPayingMana() {
        harness.addToBattlefield(player1, new HeirloomEpic());
        List<Permanent> creatures = addCreatures(4);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creature tapping can pay part of the activation cost")
    void tapsCreaturesAndPaysTheRestWithMana() {
        harness.addToBattlefield(player1, new HeirloomEpic());
        List<Permanent> creatures = addCreatures(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The activation cannot tap more creatures than mana in its cost")
    void rejectsTooManyCreatures() {
        harness.addToBattlefield(player1, new HeirloomEpic());
        List<Permanent> creatures = addCreatures(5);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, creatures.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many creatures");
        assertThat(creatures).noneMatch(Permanent::isTapped);
    }

    private List<Permanent> addCreatures(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();
    }

    @Test
    void paysEntireCostWithManaAndTapsEpicBeforeDrawing() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new Forest(), new HeirloomEpic()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        assertThat(epic.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void canTapSummoningSickCreaturesForPayment() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new BarkformHarvester()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, creatures.stream().map(Permanent::getId).toList());

        assertThat(epic.isTapped()).isTrue();
        assertThat(creatures).allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void rejectsTappedCreatureWithoutPayingOtherCosts() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(epic.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsOpponentsCreature() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(epic.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsNoncreaturePayment() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(epic.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsDuplicateCreaturePayment() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(epic.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotTapSelectedCreature() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(epic.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.currentStep = TurnStep.UPKEEP;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(epic.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOnOpponentsTurn() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.activePlayerId = player2.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(epic.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        harness.addToBattlefield(player1, new HeirloomEpic());
        Permanent secondEpic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(secondEpic.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void cannotActivateTappedEpic() {
        Permanent epic = harness.addToBattlefieldAndReturn(player1, new HeirloomEpic());
        epic.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
