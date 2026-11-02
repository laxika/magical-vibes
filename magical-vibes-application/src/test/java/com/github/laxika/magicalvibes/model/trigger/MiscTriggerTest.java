package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The triggers of the last slots to leave {@code EffectSlot}: {@link PlanarTrigger}, {@link ControlChangeTrigger},
 * {@link AttachTrigger}, {@link DayNightTrigger}, {@link StateTrigger}, and the crew, saddle, station, door and Room
 * actions of {@link ObjectActionTrigger} and {@link PlayerActionTrigger}.
 */
class MiscTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);

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
    void aStateTriggerTakesOnlyStateTriggerEffects() {
        Card card = new Card();

        assertThatThrownBy(() -> card.addTrigger(Triggers.state(), DRAW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A state trigger requires a StateTriggerEffect");
        assertThat(card.getTriggeredAbilities()).isEmpty();
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
                Triggers.doorUnlocked(), Triggers.state());
    }
}
