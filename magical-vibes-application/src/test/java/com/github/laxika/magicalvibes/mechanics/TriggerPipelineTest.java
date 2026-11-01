package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abilities with a real trigger go through the trigger pipeline: matched against the rules events of their
 * source's own zone changes and put on the stack with their options and targets.
 */
class TriggerPipelineTest extends BaseCardTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(3);

    @BeforeEach
    void emptyHands() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    @Test
    void aDiesTriggerGoesOnTheStackAndResolves() {
        Permanent bears = battlefieldCreatureWith(Triggers.dies(ObjectRelation.THIS), DRAW);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        StackEntry trigger = onlyTriggerOf(bears.getCard());
        assertThat(trigger.getEffectsToResolve()).containsExactly(DRAW);
        assertThat(trigger.getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize - 1);
    }

    @Test
    void aDiesTriggerDoesNotTriggerWhenThePermanentIsBounced() {
        Permanent bears = battlefieldCreatureWith(Triggers.dies(ObjectRelation.THIS), DRAW);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));

        assertThat(triggersOf(bears.getCard())).isEmpty();
    }

    @Test
    void aLeavesTheBattlefieldTriggerTriggersForAnyDestination() {
        Permanent bounced = battlefieldCreatureWith(Triggers.leavesBattlefield(ObjectRelation.THIS), GAIN);
        Permanent exiled = battlefieldCreatureWith(Triggers.leavesBattlefield(ObjectRelation.THIS), GAIN);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bounced));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, exiled));

        assertThat(triggersOf(bounced.getCard())).hasSize(1);
        assertThat(triggersOf(exiled.getCard())).hasSize(1);
    }

    @Test
    void aDeathAReplacementExilesInsteadDoesNotTriggerDies() {
        harness.addToBattlefield(player2, new RestInPeace());
        Permanent bears = battlefieldCreatureWith(Triggers.dies(ObjectRelation.THIS), DRAW);
        Card leaves = new GrizzlyBears();
        leaves.addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), GAIN);
        Permanent other = harness.addToBattlefieldAndReturn(player1, leaves);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other));

        assertThat(triggersOf(bears.getCard())).isEmpty();
        assertThat(triggersOf(leaves)).hasSize(1);
    }

    @Test
    void aPermanentThatLostItsAbilitiesHasNoDiesTrigger() {
        Permanent bears = battlefieldCreatureWith(Triggers.dies(ObjectRelation.THIS), DRAW);
        bears.setLosesAllAbilitiesUntilEndOfTurn(true);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(triggersOf(bears.getCard())).isEmpty();
    }

    @Test
    void aCardPutIntoAGraveyardFromHandTriggersFromTheGraveyardUnderItsOwnersControl() {
        Card card = new GrizzlyBears();
        card.addTrigger(Triggers.putIntoGraveyardFrom(Zone.HAND, ObjectRelation.THIS), DRAW);
        card.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(card));

        harness.inMutationScope(() -> moveCard(player2, card, Zone.HAND, Zone.GRAVEYARD));

        StackEntry trigger = onlyTriggerOf(card);
        assertThat(trigger.getControllerId()).isEqualTo(player2.getId());
        assertThat(trigger.getTriggeringCardId()).isEqualTo(card.getId());
    }

    @Test
    void aFalseInterveningIfKeepsItFromTriggeringAndATrueOneIsCheckedAgainOnResolution() {
        Permanent unmet = battlefieldCreatureWith(new TriggerCondition(Triggers.dies(ObjectRelation.THIS)),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerLifeAtLeast(40, false)), DRAW);
        Permanent met = battlefieldCreatureWith(new TriggerCondition(Triggers.dies(ObjectRelation.THIS)),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerLifeAtLeast(1, false)), DRAW);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, unmet));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, met));

        assertThat(triggersOf(unmet.getCard())).isEmpty();
        assertThat(onlyTriggerOf(met.getCard()).getEffectsToResolve())
                .containsExactly(new ConditionalEffect(new ControllerLifeAtLeast(1, false), DRAW, true));
    }

    @Test
    void aTriggerLimitedToOnceEachTurnTriggersOnlyOnceThatTurn() {
        Card card = new GrizzlyBears();
        card.addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        harness.setHand(player1, List.of(card));

        harness.inMutationScope(() -> {
            moveCard(player1, card, Zone.HAND, Zone.GRAVEYARD);
            moveCard(player1, card, Zone.GRAVEYARD, Zone.HAND);
            moveCard(player1, card, Zone.HAND, Zone.GRAVEYARD);
        });

        assertThat(triggersOf(card)).hasSize(1);
    }

    @Test
    void aTargetedTriggerWaitsForItsTarget() {
        Permanent bears = battlefieldCreatureWith(Triggers.dies(ObjectRelation.THIS),
                new DealDamageToAnyTargetEffect(2));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(triggersOf(bears.getCard())).isEmpty();
        assertThat(gd.pendingInteractions).anySatisfy(interaction -> {
            assertThat(interaction).isInstanceOf(PermanentChoiceContext.DeathTriggerTarget.class);
            PermanentChoiceContext.DeathTriggerTarget target = (PermanentChoiceContext.DeathTriggerTarget) interaction;
            assertThat(target.dyingCard().getId()).isEqualTo(bears.getCard().getId());
            assertThat(target.effects()).containsExactly(new DealDamageToAnyTargetEffect(2));
        });
    }

    @Test
    void oneDeathPutsTheLeavesTriggerOnTheStackBeforeTheDiesTrigger() {
        Card card = new GrizzlyBears();
        card.addTrigger(Triggers.dies(ObjectRelation.THIS), DRAW);
        card.addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), GAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(triggersOf(card)).extracting(StackEntry::getEffectsToResolve)
                .containsExactly(List.of(GAIN), List.of(DRAW));
    }

    @Test
    void aDiesTriggerOfSeveralEffectsIsOneStackObject() {
        Card card = new GrizzlyBears();
        card.addTrigger(Triggers.dies(ObjectRelation.THIS), DRAW, GAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(onlyTriggerOf(card).getEffectsToResolve()).containsExactly(DRAW, GAIN);
    }

    /** Moves {@code card} straight between two of {@code player}'s zone lists, which report the move. */
    private void moveCard(Player player, Card card, Zone from, Zone to) {
        zone(player, from).remove(card);
        zone(player, to).add(card);
    }

    private List<Card> zone(Player player, Zone zone) {
        return zone == Zone.HAND ? gd.playerHands.get(player.getId()) : gd.playerGraveyards.get(player.getId());
    }

    private Permanent battlefieldCreatureWith(ZoneChangeTrigger trigger, CardEffect effect) {
        return battlefieldCreatureWith(new TriggerCondition(trigger), TriggerOptions.DEFAULT, effect);
    }

    private Permanent battlefieldCreatureWith(TriggerCondition condition, TriggerOptions options, CardEffect effect) {
        Card card = new GrizzlyBears();
        card.addTrigger(condition, options, effect);
        return harness.addToBattlefieldAndReturn(player1, card);
    }

    private List<StackEntry> triggersOf(Card card) {
        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getId().equals(card.getId()))
                .toList();
    }

    private StackEntry onlyTriggerOf(Card card) {
        List<StackEntry> triggers = triggersOf(card);
        assertThat(triggers).hasSize(1);
        return triggers.getFirst();
    }
}
