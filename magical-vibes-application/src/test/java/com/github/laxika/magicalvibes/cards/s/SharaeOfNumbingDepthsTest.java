package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SharaeOfNumbingDepths.class, IcyManipulator.class, GrizzlyBears.class})
class SharaeOfNumbingDepthsTest extends BaseCardTest {

    @Test
    void entersByTappingTargetAndGivingItAStunCounter() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SharaeOfNumbingDepths()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void tappingAnOpponentsCreatureDrawsOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new SharaeOfNumbingDepths());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, firstOpponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, secondOpponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void tappingAnAlreadyTappedOpponentCreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new SharaeOfNumbingDepths());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void cannotTargetYourOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SharaeOfNumbingDepths()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entryTapDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SharaeOfNumbingDepths()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void entryAddsStunCounterToAlreadyTappedCreatureWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SharaeOfNumbingDepths()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void tappingNoncreatureDoesNotUseTheOncePerTurnTrigger() {
        harness.addToBattlefield(player1, new SharaeOfNumbingDepths());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        harness.activateAbility(player1, 2, null, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void opponentTappingTheirOwnCreatureDoesNotDrawForSharae() {
        harness.addToBattlefield(player1, new SharaeOfNumbingDepths());
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void tappingYourOwnCreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new SharaeOfNumbingDepths());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
