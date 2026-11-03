package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SchemeSetInMotionTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringRoomDoorConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The triggers of the last slots to leave {@code EffectSlot}: {@link PlanarTrigger}, {@link ControlChangeTrigger},
 * {@link AttachTrigger}, {@link DayNightTrigger}, {@link StateTrigger}, and the crew, saddle, station, door and Room
 * actions of {@link ObjectActionTrigger} and {@link PlayerActionTrigger}; and the legacy view of their options and
 * filters (step 14, run 15).
 */
class MiscTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);
    private static final PermanentPredicate FLYING = new PermanentHasKeywordPredicate(Keyword.FLYING);
    private static final CardPredicate LAND = new CardTypePredicate(CardType.LAND);

    @Test
    void planarTriggersFunctionFromTheCommandZoneAndTheOthersFromTheBattlefield() {
        assertThat(planarFactories()).allSatisfy(trigger ->
                assertThat(trigger.defaultZones()).isEqualTo(AbilityZones.COMMAND));
        assertThat(otherFactories()).allSatisfy(trigger ->
                assertThat(trigger.defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD));
    }

    @Test
    void everyFactoryNamesItsOwnTrigger() {
        assertThat(Stream.concat(planarFactories().stream(), otherFactories().stream()))
                .doesNotHaveDuplicates();
    }

    @Test
    void namesTheEventAndWhoItHappensTo() {
        assertThat(Triggers.youLoseControlOfThis()).isEqualTo(new ControlChangeTrigger(
                ControlChangeTrigger.ControlEvent.YOU_LOSE, ObjectRelation.THIS));
        assertThat(Triggers.opponentGainsControlOfPermanentYouControl()).isEqualTo(new ControlChangeTrigger(
                ControlChangeTrigger.ControlEvent.OPPONENT_GAINS, ObjectRelation.YOU_CONTROL));
        assertThat(Triggers.crewsVehicle()).isEqualTo(new ObjectActionTrigger(
                ObjectActionTrigger.ObjectAction.CREWS_VEHICLE, ObjectRelation.THIS));
        assertThat(Triggers.creatureYouControlCrewsVehicle()).isEqualTo(new ObjectActionTrigger(
                ObjectActionTrigger.ObjectAction.CREWS_VEHICLE, ObjectRelation.YOU_CONTROL));
        assertThat(Triggers.youFullyUnlockRoom()).isEqualTo(new PlayerActionTrigger(
                PlayerActionTrigger.PlayerAction.FULLY_UNLOCKS_ROOM, PlayerRelation.YOU));
        assertThat(Triggers.chaosEnsues()).isEqualTo(new PlanarTrigger(PlanarTrigger.PlanarEvent.CHAOS_ENSUES));
        assertThat(Triggers.state()).isEqualTo(new StateTrigger());
        assertThat(Triggers.dayBecomesNightOrNightBecomesDay()).isEqualTo(new DayNightTrigger());
    }

    @Test
    void needsAnEventAndWhoItHappensTo() {
        assertThatThrownBy(() -> new PlanarTrigger(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new AttachTrigger(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ControlChangeTrigger(null, ObjectRelation.THIS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ControlChangeTrigger(ControlChangeTrigger.ControlEvent.YOU_LOSE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsTheseAbilitiesByTriggerAndZone() {
        Card card = new Card();
        card.addTrigger(Triggers.dayBecomesNightOrNightBecomesDay(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.dayBecomesNightOrNightBecomesDay(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, GAIN);
        card.addTrigger(Triggers.chaosEnsues(), DRAW, GAIN);

        assertThat(card.getTriggerEffects(Triggers.dayBecomesNightOrNightBecomesDay(), Zone.BATTLEFIELD))
                .containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.dayBecomesNightOrNightBecomesDay(), Zone.GRAVEYARD))
                .containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.chaosEnsues(), Zone.COMMAND)).containsExactly(DRAW, GAIN);
        assertThat(card.getTriggerEffects(Triggers.chaosEnsues())).containsExactly(DRAW, GAIN);
        assertThat(card.getTriggerEffects(Triggers.chaosEnsues(), Zone.BATTLEFIELD)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.encounterThis())).isEmpty();
    }

    @Test
    void aStateTriggerNeedsItsGameStateAndNoStateTriggerEffect() {
        Card card = new Card();

        assertThatThrownBy(() -> card.addTrigger(Triggers.state(), DRAW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A state trigger needs its game state");
        assertThatThrownBy(() -> card.addTrigger(Triggers.sourceState(FLYING),
                new StateTriggerEffect(FLYING, List.of(DRAW), "Old form")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a StateTriggerEffect");
        assertThat(card.getTriggeredAbilities()).isEmpty();
    }

    @Test
    void aStateTriggerIsWatchedWithoutItsGameStateAndReadAsOneStateTriggerEffect() {
        StateTrigger state = Triggers.battlefieldHasAtMost(0, FLYING).referencing(FLYING);
        TriggeredAbility ability = new TriggeredAbility(state,
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW, GAIN);
        Card card = new Card();
        card.addTriggeredAbility(ability);

        assertThat(state.watchKey()).isEqualTo(Triggers.state());
        assertThat(ability.legacyEffects()).singleElement().satisfies(registration ->
                assertThat(registration.effect()).isEqualTo(new StateTriggerEffect(null, null, FLYING, 0, 0, FLYING,
                        List.of(new ConditionalEffect(new ControllerTurn(), DRAW, true),
                                new ConditionalEffect(new ControllerTurn(), GAIN, true)), null)));
        assertThat(card.getLegacyTriggerEffects(Triggers.state(), Zone.BATTLEFIELD)).singleElement()
                .isSameAs(card.getLegacyTriggerEffects(Triggers.state(), Zone.BATTLEFIELD).getFirst());
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.sourceState(FLYING),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW).legacyEffects())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aDoorAndAnExploredCardAreDroppedFromTheWatchKeyAndReadAsTheirWrappers() {
        assertThat(Triggers.doorUnlocked(1).watchKey()).isEqualTo(Triggers.doorUnlocked());
        assertThat(new TriggeredAbility(Triggers.doorUnlocked(1), DRAW).legacyEffects())
                .extracting(EffectRegistration::effect)
                .containsExactly(new TriggeringRoomDoorConditionalEffect(1, DRAW));

        ObjectActionTrigger landExplored = Triggers.creatureYouControlExplores().withFilter(LAND);
        assertThat(landExplored.watchKey()).isEqualTo(Triggers.creatureYouControlExplores());
        assertThat(new TriggeredAbility(landExplored, DRAW).legacyEffects())
                .extracting(EffectRegistration::effect)
                .containsExactly(new TriggeringCardConditionalEffect(LAND, DRAW));
        assertThatThrownBy(() -> new TriggeredAbility(landExplored,
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW).legacyEffects())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyTheirOwnActionsTakeADoorOrAFilter() {
        assertThatThrownBy(() -> Triggers.doorUnlocked(2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.crewsVehicle().withFilter(LAND))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.youCommitCrime().withFilter(LAND))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.creatureYouControlExplores().withFilter(LAND).withFilter(LAND))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theFirstCrewOrSaddleEachTurnIsWatchedByItsEventAndReadAsTheUnkeyedMark() {
        assertThat(Triggers.becomesCrewedForFirstTimeEachTurn().watchKey()).isEqualTo(Triggers.becomesCrewed());
        assertThat(Triggers.becomesSaddledForFirstTimeEachTurn().watchKey()).isEqualTo(Triggers.becomesSaddled());
        assertThat(new TriggeredAbility(Triggers.becomesCrewedForFirstTimeEachTurn(), DRAW).legacyEffects())
                .extracting(EffectRegistration::effect)
                .containsExactly(new OncePerTurnTriggerEffect(DRAW));
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.becomesSaddledForFirstTimeEachTurn(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW).legacyEffects())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aSchemeTriggerIsReadAsOneSchemeSetInMotionTriggerEffectWithItsFilter() {
        PlayerActionTrigger scheme = Triggers.youSetSchemeInMotion().withFilter(LAND);

        assertThat(scheme.watchKey()).isEqualTo(Triggers.youSetSchemeInMotion());
        assertThat(new TriggeredAbility(scheme, DRAW, GAIN).legacyEffects())
                .extracting(EffectRegistration::effect)
                .containsExactly(new SchemeSetInMotionTriggerEffect(LAND, List.of(DRAW, GAIN)));
        assertThat(new TriggeredAbility(Triggers.youSetSchemeInMotion(), DRAW).legacyEffects())
                .extracting(EffectRegistration::effect)
                .containsExactly(new SchemeSetInMotionTriggerEffect(null, List.of(DRAW)));
        assertThatThrownBy(() -> new TriggeredAbility(scheme,
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW).legacyEffects())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aLimitIsReadAsTheKeyedMark() {
        TriggeredAbility crime = new TriggeredAbility(Triggers.youCommitCrime(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);

        assertThat(crime.legacyEffects()).extracting(EffectRegistration::effect)
                .containsExactly(OncePerTurnTriggerEffect.keyed(DRAW, crime.limitKey()));
    }

    private static List<Trigger> planarFactories() {
        return List.of(Triggers.planeswalkToThis(), Triggers.planeswalkAwayFromThis(), Triggers.chaosEnsues(),
                Triggers.encounterThis());
    }

    private static List<Trigger> otherFactories() {
        return List.of(Triggers.youGainControlOfThis(), Triggers.youLoseControlOfThis(),
                Triggers.opponentGainsControlOfPermanentYouControl(), Triggers.auraBecomesAttachedToThis(),
                Triggers.auraYouControlBecomesAttachedToCreatureYouControl(),
                Triggers.auraYouControlBecomesAttachedToNonlandPermanentOpponentControls(),
                Triggers.becomesAttachedToCreature(), Triggers.crewsVehicle(), Triggers.creatureYouControlCrewsVehicle(),
                Triggers.saddlesOrCrewsDuringMainPhase(), Triggers.becomesStationed(), Triggers.becomesCrewed(),
                Triggers.becomesSaddled(), Triggers.dayBecomesNightOrNightBecomesDay(), Triggers.youFullyUnlockRoom(),
                Triggers.doorUnlocked(), Triggers.doorUnlocked(0), Triggers.doorUnlocked(1),
                Triggers.becomesCrewedForFirstTimeEachTurn(), Triggers.becomesSaddledForFirstTimeEachTurn(),
                Triggers.state());
    }
}
