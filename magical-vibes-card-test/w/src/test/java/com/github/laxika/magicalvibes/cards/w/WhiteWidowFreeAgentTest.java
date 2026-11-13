package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteWidowFreeAgent.class, FountainOfYouth.class, GloriousAnthem.class,
        GrizzlyBears.class, Island.class})
class WhiteWidowFreeAgentTest extends BaseCardTest {

    private static final String COUNTER_MODE = "Put a +1/+1 counter on each of up to two target creatures";
    private static final String RETURN_MODE =
            "Return target artifact or enchantment card from your graveyard to your hand";

    @Test
    void counterModePutsCountersOnTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCounterMode(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterModeCannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.enterBattlefieldAndReturn(player1, new WhiteWidowFreeAgent());
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterModeCanTargetOneOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCounterMode(List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterModeStillAffectsRemainingTargetWhenOtherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCounterMode(List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringWithoutBeingCastAllowsChoosingReturnMode() {
        Card artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));

        harness.enterBattlefieldAndReturn(player1, new WhiteWidowFreeAgent());
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void counterModeCanTargetWhiteWidowItself() {
        Permanent widow = harness.enterBattlefieldAndReturn(player1, new WhiteWidowFreeAgent());
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, widow.getId());
        harness.passBothPriorities();

        assertThat(widow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterModeCanChooseNoTargets() {
        Permanent widow = harness.enterBattlefieldAndReturn(player1, new WhiteWidowFreeAgent());
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(widow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "White Widow, Free Agent");
    }

    @Test
    void returnModeReturnsArtifactCard() {
        Card artifact = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, artifact));

        castReturnMode();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(artifact.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnModeReturnsEnchantmentCard() {
        Card enchantment = new GloriousAnthem();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, enchantment));

        castReturnMode();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(enchantment.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castCounterMode(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WhiteWidowFreeAgent()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTER_MODE);
        for (java.util.UUID targetId : targetIds) {
            harness.handlePermanentChosen(player1, targetId);
        }
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
    }

    private void castReturnMode() {
        harness.setHand(player1, List.of(new WhiteWidowFreeAgent()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
