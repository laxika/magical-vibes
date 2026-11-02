package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoonlitWake;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abilities with a real trigger go through the trigger pipeline: matched against the rules events of their
 * source's own zone changes, of another object dying or entering, and put on the stack with their options and
 * targets.
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
    void aLimitCoversEveryTriggerConditionOfTheAbility() {
        Card card = new GrizzlyBears();
        card.addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.HAND, ObjectRelation.THIS)),
                new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, ObjectRelation.THIS))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(DRAW, TriggerMode.NORMAL))));
        harness.setHand(player1, List.of(card));

        harness.inMutationScope(() -> {
            moveCard(player1, card, Zone.HAND, Zone.GRAVEYARD);
            moveCard(player1, card, Zone.GRAVEYARD, Zone.LIBRARY);
            moveCard(player1, card, Zone.LIBRARY, Zone.GRAVEYARD);
        });

        // "This ability triggers only once each turn" is about the one ability, whichever condition it met.
        assertThat(triggersOf(card)).hasSize(1);
    }

    @Test
    void aWhenThisDiesFilterIsJudgedOnThePermanentAsItLastExisted() {
        ZoneChangeTrigger diesWithCounter = Triggers.dies(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE), ObjectRelation.THIS);
        Permanent countered = battlefieldCreatureWith(diesWithCounter, DRAW);
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent plain = battlefieldCreatureWith(diesWithCounter, DRAW);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, countered));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, plain));

        // CR 603.10a: the counter left the game with the creature, but the ability looks back at it.
        assertThat(onlyTriggerOf(countered.getCard()).getEffectsToResolve()).containsExactly(DRAW);
        assertThat(triggersOf(plain.getCard())).isEmpty();
    }

    @Test
    void aWhenThisDiesIfItWasACreatureFilterReadsTheTypesItDiedWith() {
        ZoneChangeTrigger diesAsCreature = Triggers.dies(Triggers.CREATURE, ObjectRelation.THIS);
        Permanent creature = battlefieldCreatureWith(diesAsCreature, DRAW);
        Card enchantment = new Opalescence();
        enchantment.addTrigger(diesAsCreature, DRAW);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, enchantment);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                noncreature));

        assertThat(triggersOf(creature.getCard())).hasSize(1);
        assertThat(triggersOf(enchantment)).isEmpty();
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

    @Test
    void aCreatureYouControlAbilityTriggersForItsSourcesOwnDeath() {
        Permanent bears = battlefieldCreatureWith(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), GAIN);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        // It was a creature its controller controlled as it died (CR 603.10a).
        assertThat(onlyTriggerOf(bears.getCard()).getEffectsToResolve()).containsExactly(GAIN);
    }

    @Test
    @CardUsed({Opalescence.class, MoonlitWake.class})
    void anAnimatedNoncreatureSourceTriggersForItsOwnDeath() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent wake = harness.addToBattlefieldAndReturn(player1, new MoonlitWake());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wake));

        // Opalescence made it a creature, so "whenever a creature dies" sees it die (CR 603.10a).
        onlyTriggerOf(wake.getCard());
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    void anAnotherCreatureAbilityDoesNotTriggerForItsSourcesOwnDeath() {
        Permanent watcher = battlefieldCreatureWith(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), GAIN);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other));
        assertThat(triggersOf(watcher.getCard())).hasSize(1);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, watcher));
        assertThat(triggersOf(watcher.getCard())).isEmpty();
    }

    @Test
    @CardUsed({MoonlitWake.class, PlanarCleansing.class})
    void aWatcherDestroyedWithTheCreaturesItWatchesTriggersForEachOfThem() {
        Permanent wake = harness.addToBattlefieldAndReturn(player1, new MoonlitWake());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        // CR 603.10a's example: the watcher goes to the graveyard at the same time as the two creatures, and its
        // ability still triggers once for each of them.
        harness.assertInGraveyard(player1, "Moonlit Wake");
        assertThat(triggersOf(wake.getCard())).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    void aCreatureYouControlEntersAbilityTriggersForItsOwnEntryAndAnAnotherOneDoesNot() {
        Card self = new GrizzlyBears();
        self.addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL), GAIN);
        Card other = new GrizzlyBears();
        other.addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), DRAW);
        harness.setHand(player1, List.of(self, other));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        // CR 603.6a: the permanent that entered is checked for enters abilities too.
        assertThat(triggersOf(self)).hasSize(1);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(triggersOf(self)).hasSize(1);
        assertThat(triggersOf(other)).isEmpty();
    }

    @Test
    void aWhenThisEntersAbilityTriggersOnceForItsOwnEntryAndNotForAnotherPermanents() {
        Card self = new GrizzlyBears();
        self.addTrigger(Triggers.enters(ObjectRelation.THIS), GAIN);
        harness.setHand(player1, List.of(self, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        onlyTriggerOf(self);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(triggersOf(self)).isEmpty();
    }

    @Test
    void plainWhenThisEntersAbilitiesShareOneStackObjectAndAnIndependentOneHasItsOwn() {
        Card card = new GrizzlyBears();
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), DRAW);
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), GAIN);
        CardEffect independent = new GainLifeEffect(1);
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, independent);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(triggersOf(card)).extracting(StackEntry::getEffectsToResolve)
                .containsExactlyInAnyOrder(List.of(DRAW, GAIN), List.of(independent));
    }

    @Test
    @CardUsed(RaiseTheAlarm.class)
    void aOneOrMoreTokensAbilityTriggersOnceForTheTokensOneCreationMakes() {
        Card watcher = new GrizzlyBears();
        watcher.addTrigger(new TriggerCondition(Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, GAIN);
        harness.addToBattlefield(player1, watcher);
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // CR 603.2c: the two tokens enter in one event.
        onlyTriggerOf(watcher);
    }

    /** Moves {@code card} straight between two of {@code player}'s zone lists, which report the move. */
    private void moveCard(Player player, Card card, Zone from, Zone to) {
        zone(player, from).remove(card);
        zone(player, to).add(card);
    }

    private List<Card> zone(Player player, Zone zone) {
        return switch (zone) {
            case HAND -> gd.playerHands.get(player.getId());
            case LIBRARY -> gd.playerDecks.get(player.getId());
            default -> gd.playerGraveyards.get(player.getId());
        };
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
