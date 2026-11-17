package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The planar, control-change, attach, day and night, state and action triggers: {@link PlanarTrigger},
 * {@link ControlChangeTrigger}, {@link AttachTrigger}, {@link DayNightTrigger}, {@link StateTrigger}, and the crew, saddle, station, door and Room
 * actions of {@link ObjectActionTrigger} and {@link PlayerActionTrigger}: what each names, what its watch key drops
 * for its matcher to judge, and that none of them has a legacy view.
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
    void noAbilityOfTheseTriggersHasALegacyView() {
        List<Trigger> triggers = Stream.concat(planarFactories().stream(), otherFactories().stream())
                .map(trigger -> trigger.equals(Triggers.state()) ? Triggers.sourceState(FLYING) : trigger).toList();
        assertThat(triggers).allSatisfy(trigger -> {
            TriggeredAbility ability = new TriggeredAbility(trigger, DRAW);
            assertThat(ability.hasOwnEntry()).as(trigger.toString()).isFalse();
            assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        });
        Card card = new Card();
        card.addTrigger(Triggers.doorUnlocked(1), DRAW);
        assertThat(card.getTriggerEffects(Triggers.doorUnlocked(1))).containsExactly(DRAW);
    }

    @Test
    void anEerieAbilityHasNoLegacyViewAndIsWatchedForTheUnlock() {
        TriggeredAbility eerie = new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.enters(ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.youFullyUnlockRoom())), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(DRAW)));

        assertThat(eerie.hasOwnEntry()).isFalse();
        assertThatThrownBy(eerie::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThat(eerie.hasWatchedTrigger(Triggers.youFullyUnlockRoom())).isTrue();
    }

    @Test
    void aStateTriggerIsWatchedWithoutItsGameState() {
        assertThat(Triggers.battlefieldHasAtMost(0, FLYING).referencing(FLYING).watchKey())
                .isEqualTo(Triggers.state());
        assertThat(Triggers.sourceState(FLYING).watchKey()).isEqualTo(Triggers.state());
    }

    @Test
    void aStateTriggerTakesOnlyAnInterveningIf() {
        PermanentPredicate enchantment = new PermanentIsEnchantmentPredicate();
        assertThat(List.of(TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()))).allSatisfy(options ->
                assertThatThrownBy(() -> new TriggeredAbility(Triggers.sourceState(FLYING), options, DRAW))
                        .as(options.toString()).isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("state-trigger reader"));
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.sourceState(FLYING), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new TriggeredAbility(Triggers.sourceState(FLYING), TriggerOptions.DEFAULT.withInterveningIf(
                new SourceMatchesPermanentPredicate(enchantment)),
                DRAW).options().interveningIf()).isNotNull();
    }

    @Test
    void aDoorAndAnExploreFilterAreDroppedFromTheWatchKey() {
        assertThat(Triggers.doorUnlocked(1).watchKey()).isEqualTo(Triggers.doorUnlocked());
        ObjectActionTrigger landExplored = Triggers.creatureYouControlExplores().withFilter(LAND);
        assertThat(landExplored.watchKey()).isEqualTo(Triggers.creatureYouControlExplores());
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
    void aCrewObjectFilterIsDroppedFromTheWatchKey() {
        ObjectActionTrigger flyingCrews = Triggers.creatureYouControlCrewsVehicle().withObjectFilter(FLYING);

        assertThat(flyingCrews.watchKey()).isEqualTo(Triggers.creatureYouControlCrewsVehicle());
        assertThatThrownBy(() -> Triggers.becomesCrewed().withObjectFilter(FLYING))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> flyingCrews.withObjectFilter(FLYING)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theFirstCrewOrSaddleEachTurnIsWatchedByItsEvent() {
        assertThat(Triggers.becomesCrewedForFirstTimeEachTurn().watchKey()).isEqualTo(Triggers.becomesCrewed());
        assertThat(Triggers.becomesSaddledForFirstTimeEachTurn().watchKey()).isEqualTo(Triggers.becomesSaddled());
    }

    @Test
    void noAbilityOfTheseTriggersTakesOneOrMore() {
        assertThat(otherFactories().stream().filter(trigger -> !trigger.equals(Triggers.state())).toList())
                .allSatisfy(trigger -> assertThatThrownBy(() -> new TriggeredAbility(trigger,
                        TriggerOptions.ONE_OR_MORE, DRAW)).as(trigger.toString())
                        .isInstanceOf(IllegalArgumentException.class));
    }

    @Test
    void aSchemeTriggerIsWatchedWithoutItsFilter() {
        assertThat(Triggers.youSetSchemeInMotion().withFilter(LAND).watchKey())
                .isEqualTo(Triggers.youSetSchemeInMotion());
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
                Triggers.permanentEnteringCausesAbilityToTrigger(), Triggers.state());
    }
}
