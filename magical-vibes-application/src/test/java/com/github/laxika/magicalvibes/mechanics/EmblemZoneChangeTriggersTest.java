package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An emblem's zone-change abilities trigger from the command zone (CR 114.4) through the trigger pipeline, as any
 * other object's do: the emblem is its controller's, its source the card the emblem was made of, and the event's
 * object is recorded on the stack entry whatever the effects are.
 */
class EmblemZoneChangeTriggersTest extends BaseCardTest {

    @Test
    @DisplayName("An emblem's enters ability goes on the stack as written with the entering permanent as \"it\"")
    void emblemEntersAbilityGoesOnTheStackWithTheEnteringPermanent() {
        MayEffect draw = new MayEffect(new DrawCardEffect(1), "Draw a card?");
        Card source = emblemOf(player1.getId(), new TriggeredAbility(
                Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), draw));

        Permanent land = harness.enterLandBattlefieldAndReturn(player1, new Forest());

        assertThat(entriesOf(source)).singleElement().satisfies(entry -> {
            assertThat(entry.getDescription()).isEqualTo("Planeswalker's emblem");
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(draw);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(land.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("An emblem's \"you control\" enters ability ignores a permanent an opponent controls, and its filter")
    void emblemEntersAbilityIgnoresAnOpponentsPermanentAndANonMatchingOne() {
        Card source = emblemOf(player1.getId(), new TriggeredAbility(
                Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new DrawCardEffect(1)));

        harness.enterLandBattlefieldAndReturn(player2, new Forest());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(entriesOf(source)).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("A targeted emblem enters ability queues its target choice carrying the entering permanent")
    void targetedEmblemEntersAbilityQueuesItsTargetChoice() {
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(3);
        Card source = emblemOf(player1.getId(), new TriggeredAbility(
                Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL), damage));

        Permanent artifact = harness.enterBattlefieldAndReturn(player1, new Ornithopter());

        assertThat(entriesOf(source)).isEmpty();
        // The stack door parks the ability for its target, chosen as it goes on the stack (CR 603.3d).
        PermanentChoiceContext.AttackTriggerTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThat(choice.sourceCard()).isSameAs(source);
        assertThat(choice.controllerId()).isEqualTo(player1.getId());
        assertThat(choice.effects()).containsExactly(damage);
        assertThat(choice.triggeringPermanentId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("An emblem's \"put into your graveyard from the battlefield\" ability records the card and its "
            + "last-known permanent, and ignores a permanent an opponent owns")
    void emblemPutIntoYourGraveyardFromBattlefieldAbilityRecordsTheCard() {
        RegisterDelayedReturnDyingCreatureUnderControlEffect returnIt =
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true);
        Card source = emblemOf(player1.getId(), new TriggeredAbility(
                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN),
                returnIt));
        Permanent opponentsArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opponentsArtifact));
        assertThat(entriesOf(source)).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));

        assertThat(entriesOf(source)).singleElement().satisfies(entry -> {
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
            assertThat(entry.getEffectsToResolve()).containsExactly(returnIt);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(artifact.getOriginalCard().getId());
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(artifact.getId());
        });
    }

    @Test
    @DisplayName("An emblem's \"put into your graveyard from the battlefield\" ability sees a sacrificed artifact as it "
            + "last existed, its counters included")
    void emblemPutIntoYourGraveyardFromBattlefieldAbilitySeesASacrificedArtifactAsItLastExisted() {
        Card source = emblemOf(player1.getId(), new TriggeredAbility(
                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN),
                new DrawCardEffect(1)));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setCounterCount(CounterType.CHARGE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, artifact));

        assertThat(entriesOf(source)).singleElement().satisfies(entry ->
                assertThat(entry.getTriggeringPermanentSnapshot().getCounterCount(CounterType.CHARGE)).isEqualTo(2));
    }

    @Test
    @DisplayName("An emblem's targeted \"whenever a creature dies\" ability queues its target choice with its own "
            + "target filter, and its \"put into your graveyard\" ability doesn't see a creature")
    void targetedEmblemCreatureDeathAbilityQueuesItsTargetChoice() {
        GivePoisonCountersEffect poison = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER);
        TargetFilter opponent = TargetFilters.permanentAnOpponentControls();
        Card source = emblemOf(player1.getId(), new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), poison)
                .withTargetFilter(opponent));
        Card artifactSource = emblemOf(player1.getId(), new TriggeredAbility(
                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN),
                new DrawCardEffect(1)));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        assertThat(entriesOf(source)).isEmpty();
        assertThat(entriesOf(artifactSource)).isEmpty();
        // The stack door parks the ability for its target, chosen as it goes on the stack (CR 603.3d).
        PermanentChoiceContext.AttackTriggerTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class);
        assertThat(choice.sourceCard()).isSameAs(source);
        assertThat(choice.controllerId()).isEqualTo(player1.getId());
        assertThat(choice.effects()).containsExactly(poison);
        assertThat(choice.triggerRecord().getTargetFilter()).isSameAs(opponent);
        assertThat(choice.triggerRecord().getTriggeringPermanentSnapshot().getId()).isEqualTo(creature.getId());
    }

    /** A planeswalker card whose emblem, controlled by {@code controllerId}, has {@code ability}. */
    private Card emblemOf(UUID controllerId, TriggeredAbility ability) {
        Card source = new Card();
        source.setName("Planeswalker");
        gd.emblems.add(new Emblem(controllerId, List.of(), source, List.of(), 0L, List.of(ability)));
        return source;
    }

    private List<StackEntry> entriesOf(Card source) {
        return gd.stack.stream().filter(entry -> entry.getCard() == source).toList();
    }
}
