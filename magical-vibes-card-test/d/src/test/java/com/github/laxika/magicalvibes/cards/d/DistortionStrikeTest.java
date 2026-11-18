package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
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

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, bear)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isNotEmpty();
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DistortionStrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, bear)).isFalse();
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
        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, bear)).isTrue();
        harness.assertInGraveyard(player1, "Distortion Strike");
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
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

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
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
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
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
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isNotEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, first)).isFalse();
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, second)).isTrue();
        harness.assertInGraveyard(player1, "Distortion Strike");
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();
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
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
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
        assertThat(gd.delayedStepAbilities(StepTrigger.Moment.UPKEEP)).isEmpty();

        harness.addToBattlefield(player1, new NestInvader());
        harness.passUntil(player2, TurnStep.UPKEEP);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
