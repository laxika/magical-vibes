package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimmervoidBasin.class, Shock.class, GrizzlyBears.class, Zombify.class, Pyrotechnics.class})
class GlimmervoidBasinTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new GlimmervoidBasin(), gd.nextTimestamp()));
    }

    @Test
    void copiesSingleTargetSpellForEachOtherLegalTargetAndKeepsCasterAsController() {
        Permanent player1Creature = harness.addToBattlefieldAndReturn(
                player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(
                player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1Creature.getId());

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(3);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(player2Creature.getId(), player1.getId(), player2.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player2.getId()));
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isFalse();
    }

    @Test
    void copiesASpellForOtherLegalGraveyardCards() {
        GrizzlyBears originalTarget = new GrizzlyBears();
        GrizzlyBears otherTarget = new GrizzlyBears();
        Zombify zombify = new Zombify();
        harness.setGraveyard(player2, List.of(originalTarget, otherTarget));
        harness.setHand(player2, List.of(zombify));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, originalTarget.getId());

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getTargetId()).isEqualTo(otherTarget.getId());
        assertThat(copies.getFirst().getTargetCardIds()).containsExactly(otherTarget.getId());
    }

    @Test
    void chaosCreatesCreatureCopyForEveryPlayerExceptItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(
                player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .hasSize(1);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .hasSize(1);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()).getFirst().getCard().isToken())
                .isTrue();
    }

    @Test
    void copiesDividedDamageSpellWithExactlyOneTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, Map.of(originalTarget.getId(), 4));
        harness.passBothPriorities();

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(3);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(otherTarget.getId(), player1.getId(), player2.getId());
        assertThat(copies).allSatisfy(copy ->
                assertThat(copy.getDamageAssignments()).containsExactly(Map.entry(copy.getTargetId(), 4)));
    }

    @Test
    void doesNotCopyDividedDamageSpellWithTwoTargets() {
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 2, player2.getId(), 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isFalse();
    }

    @Test
    void chaosCopiesOpponentsCreatureForThePlanarController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getName()).isEqualTo(target.getCard().getName());
                });
    }

    @Test
    void chaosDoesNotCreateTokensWhenItsTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void chaosCopiesFaceDownCharacteristicsInsteadOfTheUnderlyingCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(token -> {
                    assertThat(token.isFaceDown()).isFalse();
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getName()).isNullOrEmpty();
                    assertThat(token.getCard().getManaCost()).isNullOrEmpty();
                    assertThat(token.getCard().getSubtypes()).isEmpty();
                });
    }
}
