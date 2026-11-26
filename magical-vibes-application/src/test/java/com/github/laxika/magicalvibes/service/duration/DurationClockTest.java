package com.github.laxika.magicalvibes.service.duration;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.GrantedCardAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ResolvedCopyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.ActionEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.SpellEvent;
import com.github.laxika.magicalvibes.model.rules.StepEvent;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DurationClockTest extends BaseCardTest {

    private FloatingContinuousEffect add(UUID controllerId, Duration duration) {
        return gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Test", null, controllerId,
                BoostEffect.target(1, 1), null, null, null, duration, 0));
    }

    private List<FloatingContinuousEffect> advance(RulesEvent event) {
        return GameTestEngineContext.get().getBean(DurationClock.class).advance(gd, event);
    }

    @Test
    @DisplayName("A turn beginning ends only that player's \"until your next turn\" effects")
    void turnBeginningEndsThatPlayersEffects() {
        FloatingContinuousEffect own = add(player1.getId(), Duration.UNTIL_YOUR_NEXT_TURN);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_YOUR_NEXT_TURN);
        FloatingContinuousEffect endOfTurn = add(player1.getId(), Duration.UNTIL_END_OF_TURN);

        assertThat(advance(new TurnEvent.Began(player1.getId()))).containsExactly(own);
        assertThat(gd.floatingEffects).containsExactly(opponents, endOfTurn);
    }

    @Test
    @DisplayName("Control of a player begins with that player's next turn and ends as the turn after it begins")
    void playerControlLastsThroughTheControlledTurn() {
        gd.controlPlayer("Mindslaver", null, player1.getId(), player2.getId(),
                Duration.THROUGH_AFFECTED_PLAYERS_NEXT_TURN, false);

        advance(new TurnEvent.Began(player1.getId()));
        assertThat(gd.mindControlledPlayerId()).isNull();

        advance(new TurnEvent.Began(player2.getId()));
        assertThat(gd.mindControlledPlayerId()).isEqualTo(player2.getId());
        assertThat(gd.mindControllerPlayerId()).isEqualTo(player1.getId());

        // The controlled player is controlled through their cleanup steps (CR 724.1).
        advance(new TurnEvent.Cleanup(player2.getId()));
        assertThat(gd.mindControlledPlayerId()).isEqualTo(player2.getId());

        advance(new TurnEvent.Began(player1.getId()));
        assertThat(gd.mindControlledPlayerId()).isNull();
    }

    @Test
    @DisplayName("Control of a player during their next combat begins at that player's combat and ends with it")
    void playerControlDuringTheirNextCombat() {
        gd.controlPlayer("Secret of Bloodbending", null, player1.getId(), player2.getId(),
                Duration.DURING_AFFECTED_CONTROLLERS_NEXT_COMBAT, false);

        advance(new StepEvent(StepTrigger.Moment.BEGINNING_OF_COMBAT, player1.getId()));
        assertThat(gd.mindControlledPlayerId()).isNull();

        advance(new StepEvent(StepTrigger.Moment.BEGINNING_OF_COMBAT, player2.getId()));
        assertThat(gd.mindControlledPlayerId()).isEqualTo(player2.getId());

        advance(new TurnEvent.StepEnded(TurnStep.END_OF_COMBAT, player2.getId()));
        assertThat(gd.mindControlledPlayerId()).isNull();
    }

    @Test
    @DisplayName("A newer control effect on a player overwrites the older one; an older extra turn stays with a new one")
    void newerPlayerControlOverwritesTheOlder() {
        gd.controlPlayer("Emrakul, the Promised End", null, player1.getId(), player2.getId(),
                Duration.THROUGH_AFFECTED_PLAYERS_NEXT_TURN, true);
        gd.controlPlayer("Emrakul, the Promised End", null, player1.getId(), player2.getId(),
                Duration.THROUGH_AFFECTED_PLAYERS_NEXT_TURN, true);
        assertThat(gd.pendingTurnControlExtraTurns()).containsExactly(player2.getId(), player2.getId());

        gd.controlPlayer("Secret of Bloodbending", null, player1.getId(), player2.getId(),
                Duration.DURING_AFFECTED_CONTROLLERS_NEXT_COMBAT, false);

        assertThat(gd.pendingTurnControl()).isEmpty();
        assertThat(gd.pendingTurnControlExtraTurns()).isEmpty();
        assertThat(gd.pendingCombatControl()).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("The combat phase ending ends \"until end of combat\" effects; another step ending ends none")
    void combatEndingEndsEndOfCombatEffects() {
        FloatingContinuousEffect combat = add(player1.getId(), Duration.UNTIL_END_OF_COMBAT);

        assertThat(advance(new TurnEvent.StepEnded(TurnStep.DECLARE_BLOCKERS, player1.getId()))).isEmpty();
        assertThat(advance(new TurnEvent.StepEnded(TurnStep.END_OF_COMBAT, player1.getId()))).containsExactly(combat);
    }

    @Test
    @DisplayName("An upkeep ending ends its player's effects that last through the end of their next upkeep (CR 500.5)")
    void upkeepEndingEndsEffectsBegunBeforeIt() {
        FloatingContinuousEffect before = add(player1.getId(), Duration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP);
        gd.upkeepStartTimestamp = gd.timestampCounter + 1;
        FloatingContinuousEffect during = add(player1.getId(), Duration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP);

        assertThat(advance(new TurnEvent.StepEnded(TurnStep.UPKEEP, player1.getId()))).containsExactly(before);
        assertThat(gd.floatingEffects).containsExactly(opponents, during);
    }

    @Test
    @DisplayName("The cleanup step ends \"until end of turn\" effects and the active player's \"until the end of your"
            + " next turn\" effects begun before this turn (CR 514.2)")
    void cleanupEndsEndOfTurnEffects() {
        FloatingContinuousEffect endOfTurn = add(player2.getId(), Duration.UNTIL_END_OF_TURN);
        FloatingContinuousEffect lastTurns = add(player1.getId(), Duration.UNTIL_END_OF_YOUR_NEXT_TURN);
        gd.turnStartTimestamp = gd.timestampCounter + 1;
        FloatingContinuousEffect thisTurns = add(player1.getId(), Duration.UNTIL_END_OF_YOUR_NEXT_TURN);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_END_OF_YOUR_NEXT_TURN);
        FloatingContinuousEffect lasting = add(player1.getId(), Duration.WHILE_SOURCE_ON_BATTLEFIELD);

        assertThat(advance(new TurnEvent.Cleanup(player1.getId()))).containsExactly(endOfTurn, lastTurns);
        assertThat(gd.floatingEffects).containsExactly(thisTurns, opponents, lasting);
    }

    @Test
    @DisplayName("An upkeep beginning ends its player's \"until your next upkeep\" effects")
    void upkeepBeginningEndsThatPlayersEffects() {
        FloatingContinuousEffect own = add(player1.getId(), Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP);

        assertThat(advance(new StepEvent(StepTrigger.Moment.UPKEEP, player1.getId()))).containsExactly(own);
        assertThat(gd.floatingEffects).containsExactly(opponents);
    }

    @Test
    @DisplayName("The end step beginning ends the effects that last until the next end step")
    void endStepEndsNextEndStepEffects() {
        FloatingContinuousEffect endStep = add(player2.getId(), Duration.UNTIL_NEXT_END_STEP);

        assertThat(advance(new StepEvent(StepTrigger.Moment.END_STEP, player1.getId()))).containsExactly(endStep);
    }

    @Test
    @DisplayName("A creature spell being cast ends the effects that last until a creature spell is cast")
    void creatureCastEndsCreatureCastEffects() {
        FloatingContinuousEffect creatureCast = add(player2.getId(), Duration.UNTIL_CREATURE_SPELL_CAST);

        assertThat(advance(new SpellEvent.Cast(new GrizzlyBears(), player1.getId(), Zone.HAND, null, false, false,
                0, 0, 0, 0))).containsExactly(creatureCast);
    }

    @Test
    @DisplayName("Planeswalking ends the effects that last until a player planeswalks")
    void planeswalkEndsPlaneswalkEffects() {
        FloatingContinuousEffect planar = add(player1.getId(), Duration.UNTIL_PLANESWALK);

        assertThat(advance(new ActionEvent.Planeswalked(player1.getId()))).containsExactly(planar);
    }

    @Test
    @DisplayName("An ending \"until end of turn\" copy effect reverts the permanent to its card before the copy")
    void endingCopyEffectRevertsTheCopy() {
        Permanent skinshifter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card bears = skinshifter.getCard();
        skinshifter.setCard(new SerraAngel());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Tilonalli's Skinshifter",
                skinshifter.getId(), player1.getId(), new ResolvedCopyEffect(skinshifter.getCard(), bears),
                skinshifter.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));

        advance(new TurnEvent.Cleanup(player1.getId()));

        assertThat(skinshifter.getCard()).isSameAs(bears);
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    @DisplayName("An ending copy effect whose permanent left the battlefield just ends")
    void endingCopyEffectOfDepartedPermanent() {
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Tilonalli's Skinshifter",
                UUID.randomUUID(), player1.getId(), new ResolvedCopyEffect(new SerraAngel(), new SerraAngel()),
                UUID.randomUUID(), null, null, Duration.UNTIL_END_OF_TURN, 0));

        assertThat(advance(new TurnEvent.Cleanup(player1.getId()))).hasSize(1);
        assertThat(gd.floatingEffects).isEmpty();
    }

    private FloatingContinuousEffect addFrom(Permanent source, Duration duration, CardEffect effect) {
        return gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Test", source.getId(),
                player1.getId(), effect, null, null, null, duration, 0));
    }

    private DurationClock clock() {
        return GameTestEngineContext.get().getBean(DurationClock.class);
    }

    @Test
    @DisplayName("Its source leaving ends every \"for as long as\" effect of that source (CR 611.2b)")
    void sourceLeavingEndsItsAsLongAsEffects() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FloatingContinuousEffect remains = addFrom(source, Duration.WHILE_SOURCE_REMAINS, BoostEffect.target(1, 1));
        FloatingContinuousEffect tapped = addFrom(source, Duration.WHILE_SOURCE_TAPPED, BoostEffect.target(1, 1));
        FloatingContinuousEffect attached = addFrom(source, Duration.WHILE_ATTACHED, BoostEffect.target(1, 1));
        FloatingContinuousEffect endOfTurn = addFrom(source, Duration.UNTIL_END_OF_TURN, BoostEffect.target(1, 1));
        FloatingContinuousEffect othersEffect = addFrom(other, Duration.WHILE_SOURCE_REMAINS,
                BoostEffect.target(1, 1));

        assertThat(clock().sourceLeft(gd, source.getId())).containsExactly(remains, tapped, attached);
        assertThat(gd.floatingEffects).containsExactly(endOfTurn, othersEffect);
    }

    @Test
    @DisplayName("Its source untapping ends only the effects that last while it remains tapped")
    void sourceUntappingEndsTappedEffects() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FloatingContinuousEffect tapped = addFrom(source, Duration.WHILE_SOURCE_TAPPED, BoostEffect.target(1, 1));
        FloatingContinuousEffect remainsTapped = addFrom(source, Duration.WHILE_SOURCE_REMAINS_TAPPED,
                BoostEffect.target(1, 1));
        FloatingContinuousEffect remains = addFrom(source, Duration.WHILE_SOURCE_REMAINS, BoostEffect.target(1, 1));

        assertThat(clock().sourceUntapped(gd, source.getId())).containsExactly(tapped, remainsTapped);
        assertThat(gd.floatingEffects).containsExactly(remains);
    }

    @Test
    @DisplayName("Its source becoming unattached ends only the effects that last while it stays attached")
    void sourceUnattachedEndsAttachedEffects() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FloatingContinuousEffect attached = addFrom(source, Duration.WHILE_ATTACHED, BoostEffect.target(1, 1));
        FloatingContinuousEffect remains = addFrom(source, Duration.WHILE_SOURCE_REMAINS, BoostEffect.target(1, 1));

        assertThat(clock().sourceUnattached(gd, source.getId())).containsExactly(attached);
        assertThat(gd.floatingEffects).containsExactly(remains);
    }

    @Test
    @DisplayName("Its source changing control ends the effects that last while their creator controls it")
    void sourceChangingControlEndsCreatorControlledEffects() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FloatingContinuousEffect controlled = addFrom(source, Duration.WHILE_SOURCE_CONTROLLED,
                BoostEffect.target(1, 1));
        FloatingContinuousEffect grant = addFrom(source, Duration.WHILE_SOURCE_ON_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.FLYING, Subjects.target(), Duration.WHILE_SOURCE_ON_BATTLEFIELD));
        FloatingContinuousEffect onBattlefield = addFrom(source, Duration.WHILE_SOURCE_ON_BATTLEFIELD,
                BoostEffect.target(1, 1));
        FloatingContinuousEffect remains = addFrom(source, Duration.WHILE_SOURCE_REMAINS, BoostEffect.target(1, 1));

        assertThat(clock().sourceChangedControl(gd, source, player1.getId())).isEmpty();
        assertThat(clock().sourceChangedControl(gd, source, player2.getId())).containsExactly(controlled, grant);
        assertThat(gd.floatingEffects).containsExactly(onBattlefield, remains);
    }

    @Test
    @DisplayName("A \"for as long as\" effect holds while its source is on the battlefield, tapped and attached as it needs")
    void holdsJudgesTheBoard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FloatingContinuousEffect tapped = addFrom(source, Duration.WHILE_SOURCE_TAPPED, BoostEffect.target(1, 1));
        FloatingContinuousEffect attached = gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Test",
                source.getId(), player1.getId(), BoostEffect.target(1, 1), enchanted.getId(), null, null,
                Duration.WHILE_ATTACHED, 0));
        FloatingContinuousEffect endOfTurn = addFrom(source, Duration.UNTIL_END_OF_TURN, BoostEffect.target(1, 1));

        assertThat(clock().holds(gd, tapped)).isFalse();
        source.tap();
        assertThat(clock().holds(gd, tapped)).isTrue();
        assertThat(clock().holds(gd, attached)).isFalse();
        source.setAttachedTo(enchanted.getId());
        assertThat(clock().holds(gd, attached)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        assertThat(clock().holds(gd, attached)).isFalse();
        assertThat(clock().holds(gd, endOfTurn)).isTrue();
    }

    @Test
    @DisplayName("A grant that lasts while its card remains exiled ends as the card leaves exile")
    void cardLeavingExileEndsWhileExiledGrant() {
        Card lesson = new GrizzlyBears();
        gd.addToExile(player1.getId(), lesson);
        FloatingContinuousEffect grant = gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Test",
                null, player1.getId(), new GrantActivatedAbilityEffect(null, Subjects.target(), null,
                Duration.WHILE_SOURCE_CARD_EXILED, lesson.getId()), null, null, null,
                Duration.WHILE_SOURCE_CARD_EXILED, 0));

        assertThat(gd.floatingEffects).containsExactly(grant);
        gd.removeFromExile(lesson.getId());
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    @DisplayName("An effect \"during your next turn\" begins as its controller's next turn begins and ends at its cleanup")
    void duringYourNextTurnBeginsAtThatTurn() {
        FloatingContinuousEffect next = add(player1.getId(), Duration.DURING_YOUR_NEXT_TURN);

        assertThat(gd.floatingEffects).isEmpty();
        assertThat(gd.scheduledFloatingEffects).containsExactly(next);
        advance(new TurnEvent.Began(player2.getId()));
        assertThat(gd.floatingEffects).isEmpty();
        advance(new TurnEvent.Began(player1.getId()));
        assertThat(gd.floatingEffects).containsExactly(next);
        assertThat(gd.scheduledFloatingEffects).isEmpty();
        advance(new TurnEvent.Cleanup(player1.getId()));
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    @DisplayName("An effect \"during target player's next turn\" begins as that player's next turn begins")
    void duringAffectedPlayersNextTurnBeginsAtThatPlayersTurn() {
        FloatingContinuousEffect taunt = gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null,
                player1.getId(), BoostEffect.target(1, 1), player2.getId(),
                Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));

        advance(new TurnEvent.Began(player1.getId()));
        assertThat(gd.floatingEffects).isEmpty();
        advance(new TurnEvent.Began(player2.getId()));
        assertThat(gd.floatingEffects).containsExactly(taunt);
    }

    @Test
    @DisplayName("An effect \"during its controller's next turn\" on a permanent begins as that controller's turn begins")
    void duringAffectedPermanentControllersNextTurnBeginsAtThatPlayersTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        FloatingContinuousEffect requirement = gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null,
                player1.getId(), BoostEffect.target(1, 1), bears.getId(),
                Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));

        advance(new TurnEvent.Began(player1.getId()));
        assertThat(gd.floatingEffects).isEmpty();
        advance(new TurnEvent.Began(player2.getId()));
        assertThat(gd.floatingEffects).containsExactly(requirement);
    }

    @Test
    @DisplayName("An effect \"during its controller's next combat phase\" begins as that player's combat begins and ends with it")
    void duringAffectedControllersNextCombatBeginsAtTheirBeginningOfCombat() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        FloatingContinuousEffect requirement = gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null,
                player1.getId(), BoostEffect.target(1, 1), bears.getId(),
                Duration.DURING_AFFECTED_CONTROLLERS_NEXT_COMBAT));

        advance(new StepEvent(StepTrigger.Moment.BEGINNING_OF_COMBAT, player1.getId()));
        assertThat(gd.floatingEffects).isEmpty();
        advance(new StepEvent(StepTrigger.Moment.BEGINNING_OF_COMBAT, player2.getId()));
        assertThat(gd.floatingEffects).containsExactly(requirement);
        assertThat(advance(new TurnEvent.StepEnded(TurnStep.END_OF_COMBAT, player2.getId())))
                .containsExactly(requirement);
    }

    @Test
    @DisplayName("A \"during that combat\" effect begins as its combat phase of the turn begins and ends with it")
    void duringThatCombatBeginsAtItsCombatPhase() {
        FloatingContinuousEffect restriction = add(player1.getId(), Duration.duringCombatPhase(2));

        gd.combatPhasesThisTurn = 1;
        advance(new StepEvent(StepTrigger.Moment.BEGINNING_OF_COMBAT, player1.getId()));
        assertThat(gd.floatingEffects).isEmpty();
        gd.combatPhasesThisTurn = 2;
        advance(new StepEvent(StepTrigger.Moment.BEGINNING_OF_COMBAT, player1.getId()));
        assertThat(gd.floatingEffects).containsExactly(restriction);
        assertThat(advance(new TurnEvent.StepEnded(TurnStep.END_OF_COMBAT, player1.getId())))
                .containsExactly(restriction);
    }

    @Test
    @DisplayName("A \"during that combat\" effect whose combat phase never began is dropped as the next turn begins")
    void duringThatCombatIsDroppedWithItsTurn() {
        add(player1.getId(), Duration.duringCombatPhase(2));

        advance(new TurnEvent.Began(player2.getId()));
        gd.combatPhasesThisTurn = 2;
        advance(new StepEvent(StepTrigger.Moment.BEGINNING_OF_COMBAT, player2.getId()));

        assertThat(gd.scheduledFloatingEffects).isEmpty();
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    @DisplayName("An effect \"after this turn\" begins as the next turn begins, keeping its timestamp")
    void afterThisTurnBeginsAtTheNextTurn() {
        FloatingContinuousEffect warp = add(player1.getId(), Duration.AFTER_THIS_TURN);
        FloatingContinuousEffect later = add(player1.getId(), Duration.PERMANENT);

        advance(new TurnEvent.Began(player2.getId()));

        assertThat(gd.floatingEffects).containsExactlyInAnyOrder(warp, later);
        assertThat(warp.timestamp()).isLessThan(later.timestamp());
    }

    @Test
    @DisplayName("A card leaving its zone ends the effects on it, begun or not")
    void cardLeavingExileEndsEffectsOnIt() {
        Card exiled = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiled);
        FloatingContinuousEffect now = gd.addFloatingEffect(FloatingContinuousEffect.onCard("Test", null,
                player1.getId(), BoostEffect.target(1, 1), exiled.getId(), Zone.EXILE, Duration.UNTIL_END_OF_TURN));
        gd.addFloatingEffect(FloatingContinuousEffect.onCard("Test", null, player1.getId(),
                BoostEffect.target(1, 1), exiled.getId(), Zone.EXILE, Duration.AFTER_THIS_TURN));

        assertThat(gd.floatingEffectsOnCard(exiled.getId(), BoostEffect.class)).containsExactly(now);
        assertThat(gd.hasFloatingEffectOnPlayer(player1.getId(), BoostEffect.class)).isFalse();
        assertThat(gd.endFloatingEffectsOnCard(exiled.getId())).containsExactly(now);
        assertThat(gd.floatingEffects).isEmpty();
        assertThat(gd.scheduledFloatingEffects).isEmpty();
    }

    @Test
    @DisplayName("An effect that lasts until the end of the upkeep it was created in ends as any upkeep ends")
    void upkeepEndingEndsEndOfUpkeepEffects() {
        FloatingContinuousEffect revealed = add(player2.getId(), Duration.UNTIL_END_OF_UPKEEP);
        FloatingContinuousEffect endOfTurn = add(player2.getId(), Duration.UNTIL_END_OF_TURN);

        assertThat(advance(new TurnEvent.StepEnded(TurnStep.UPKEEP, player1.getId()))).containsExactly(revealed);
        assertThat(gd.floatingEffects).containsExactly(endOfTurn);
    }

    @Test
    @DisplayName("A flash grant for the next sorcery waits through other spells and ends with the sorcery cast")
    void nextMatchingSpellGrantEndsWithTheMatchingCast() {
        FloatingContinuousEffect grant = gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Quicken", null,
                player1.getId(), new GrantFlashToNextMatchingSpellEffect(new CardTypePredicate(CardType.SORCERY)),
                player1.getId(), Duration.UNTIL_MATCHING_SPELL_CAST));

        assertThat(advance(new SpellEvent.Cast(new GrizzlyBears(), player1.getId(), Zone.HAND, null, false, false,
                0, 0, 0, 0))).isEmpty();
        assertThat(advance(new SpellEvent.Cast(new Shock(), player1.getId(), Zone.HAND, null, false, false,
                0, 0, 0, 0))).isEmpty();
        assertThat(gd.floatingEffects).containsExactly(grant);
        assertThat(advance(new TurnEvent.Cleanup(player1.getId()))).containsExactly(grant);
    }

    @Test
    @DisplayName("A flash grant ends with the next matching spell its controller casts, not an opponent's")
    void nextMatchingSpellGrantEndsWithItsControllersCast() {
        FloatingContinuousEffect grant = gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Quicken", null,
                player1.getId(), new GrantFlashToNextMatchingSpellEffect(new CardTypePredicate(CardType.CREATURE)),
                player1.getId(), Duration.UNTIL_MATCHING_SPELL_CAST));

        assertThat(advance(new SpellEvent.Cast(new GrizzlyBears(), player2.getId(), Zone.HAND, null, false, false,
                0, 0, 0, 0))).isEmpty();
        assertThat(advance(new SpellEvent.Cast(new GrizzlyBears(), player1.getId(), Zone.HAND, null, false, false,
                0, 0, 0, 0))).containsExactly(grant);
    }

    @Test
    @DisplayName("A card entering a hand ends the effects on it, as it becomes a new object (CR 400.7)")
    void cardEnteringAHandEndsEffectsOnIt() {
        Card card = new GrizzlyBears();
        gd.addFloatingEffect(FloatingContinuousEffect.onCard("Test", null, player1.getId(),
                GrantedCardAbilityEffect.of(GrantedCardAbilityEffect.Ability.FLASHBACK), card.getId(), Zone.GRAVEYARD,
                Duration.UNTIL_END_OF_TURN));
        assertThat(gd.hasGrantedCardAbility(card.getId(), GrantedCardAbilityEffect.Ability.FLASHBACK)).isTrue();

        gd.addCardToHand(player1.getId(), card);

        assertThat(gd.hasGrantedCardAbility(card.getId(), GrantedCardAbilityEffect.Ability.FLASHBACK)).isFalse();
    }

    @Test
    @DisplayName("A card leaving a graveyard ends only the effects on it in the graveyard, not those it got in exile")
    void cardLeavingAZoneEndsOnlyThatZonesEffects() {
        Card card = new GrizzlyBears();
        FloatingContinuousEffect flashback = gd.addFloatingEffect(FloatingContinuousEffect.onCard("Test", null,
                player1.getId(), GrantedCardAbilityEffect.of(GrantedCardAbilityEffect.Ability.FLASHBACK), card.getId(),
                Zone.GRAVEYARD, Duration.UNTIL_END_OF_TURN));
        gd.addToExile(player1.getId(), card);
        FloatingContinuousEffect permission = gd.grantExilePlayPermission(card.getId(), player1.getId(),
                Duration.PERMANENT);

        assertThat(gd.endFloatingEffectsOnCard(card.getId(), Zone.GRAVEYARD)).containsExactly(flashback);
        assertThat(gd.floatingEffects).containsExactly(permission);
    }

    @Test
    @DisplayName("The trigger pipeline runs the clock on the events it is handed")
    void pipelineRunsTheClock() {
        add(player1.getId(), Duration.UNTIL_END_OF_TURN);
        FloatingContinuousEffect lasting = add(player1.getId(), Duration.PERMANENT);

        gd.rulesEvents.occur(new TurnEvent.Cleanup(player1.getId()));

        assertThat(gd.floatingEffects).containsExactly(lasting);
    }
}
