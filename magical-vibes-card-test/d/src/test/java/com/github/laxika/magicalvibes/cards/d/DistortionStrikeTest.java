package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistortionStrike.class, GrizzlyBears.class, NestInvader.class})
class DistortionStrikeTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureAndMakesItUnblockable() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DistortionStrike card = new DistortionStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.isCantBeBlocked()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DistortionStrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.isCantBeBlocked()).isFalse();
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DistortionStrike card = new DistortionStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Distortion Strike");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new DistortionStrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new DistortionStrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.isCantBeBlocked()).isTrue();
    }

    @Test
    void illegalTargetPreventsRebound() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        DistortionStrike card = new DistortionStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Distortion Strike");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundCanChooseADifferentCreatureAndDoesNotTriggerOnOpponentsUpkeep() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        DistortionStrike card = new DistortionStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isZero();
        assertThat(first.isCantBeBlocked()).isFalse();
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Distortion Strike");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOpportunity() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        DistortionStrike card = new DistortionStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Distortion Strike");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.isCantBeBlocked()).isFalse();
    }

    @Test
    void reboundWithoutLegalTargetsLeavesCardExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        DistortionStrike card = new DistortionStrike();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Distortion Strike");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.addToBattlefield(player1, new NestInvader());
        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
