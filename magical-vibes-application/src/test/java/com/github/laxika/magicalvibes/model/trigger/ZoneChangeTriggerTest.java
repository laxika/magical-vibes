package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.service.trigger.ZoneChangeAbilityReader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ZoneChangeTrigger}'s matching and zones, and how its abilities and {@link PermanentChangeTrigger}'s are read
 * (step 14, run 16p of effect-slot-refactor.md): only "when this enters" has a legacy view, the trigger pipeline reads
 * the others' options, and the readers outside it read them through {@link ZoneChangeAbilityReader}.
 */
class ZoneChangeTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);

    @Test
    void aSourcesOwnLookBackMoveFunctionsFromTheZoneItLeft() {
        assertThat(Triggers.dies(ObjectRelation.THIS).looksBack()).isTrue();
        assertThat(Triggers.dies(ObjectRelation.THIS).defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.leavesBattlefield(ObjectRelation.THIS).defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void aSourcesOwnMoveThatDoesNotLookBackFunctionsFromTheZoneItArrivesIn() {
        assertThat(Triggers.putIntoGraveyardFrom(Zone.HAND, ObjectRelation.THIS).defaultZones())
                .isEqualTo(AbilityZones.GRAVEYARD);
        assertThat(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS).defaultZones())
                .isEqualTo(AbilityZones.GRAVEYARD);
        assertThat(Triggers.milled(ObjectRelation.THIS).defaultZones()).isEqualTo(AbilityZones.GRAVEYARD);
        assertThat(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS).looksBack()).isFalse();
    }

    @Test
    void anotherObjectsMoveFunctionsFromTheBattlefield() {
        assertThat(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.YOU_CONTROL).defaultZones())
                .isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.dies(ObjectRelation.ANOTHER).defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void matchesItsZonesAndCauses() {
        ZoneChangeTrigger dies = Triggers.dies(ObjectRelation.THIS);
        assertThat(dies.matchesMove(Zone.BATTLEFIELD, Zone.GRAVEYARD, ZoneChangeCause.DESTROY)).isTrue();
        assertThat(dies.matchesMove(Zone.BATTLEFIELD, Zone.EXILE, ZoneChangeCause.OTHER)).isFalse();
        assertThat(dies.matchesMove(Zone.HAND, Zone.GRAVEYARD, ZoneChangeCause.DISCARD)).isFalse();

        ZoneChangeTrigger sacrificed = Triggers.sacrificed(ObjectRelation.THIS);
        assertThat(sacrificed.matchesMove(Zone.BATTLEFIELD, Zone.GRAVEYARD, ZoneChangeCause.SACRIFICE)).isTrue();
        assertThat(sacrificed.matchesMove(Zone.BATTLEFIELD, Zone.GRAVEYARD, ZoneChangeCause.DESTROY)).isFalse();

        ZoneChangeTrigger anywhere = Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS);
        assertThat(anywhere.matchesMove(null, Zone.GRAVEYARD, ZoneChangeCause.OTHER)).isTrue();
        assertThat(anywhere.matchesMove(Zone.STACK, Zone.GRAVEYARD, ZoneChangeCause.OTHER)).isTrue();

        assertThat(Triggers.leavesBattlefield(ObjectRelation.THIS)
                .matchesMove(Zone.BATTLEFIELD, Zone.HAND, ZoneChangeCause.OTHER)).isTrue();
        assertThat(Triggers.milled(ObjectRelation.THIS)
                .matchesMove(Zone.LIBRARY, Zone.GRAVEYARD, ZoneChangeCause.OTHER)).isFalse();
    }

    @Test
    void aMoveInsideOneZoneIsNoTrigger() {
        assertThatThrownBy(() -> new ZoneChangeTrigger(ObjectRelation.THIS, Set.of(Zone.GRAVEYARD), Zone.GRAVEYARD,
                Set.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyTheRelationsThatCanBeTheSourceIncludeIt() {
        assertThat(ObjectRelation.THIS.includesThis()).isTrue();
        assertThat(ObjectRelation.ANY.includesThis()).isTrue();
        assertThat(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL.includesThis()).isTrue();
        assertThat(ObjectRelation.ANOTHER.includesThis()).isFalse();
        assertThat(ObjectRelation.EQUIPPED.includesThis()).isFalse();
    }

    @Test
    void onlyWhenThisEntersIsReadAsItsEffects() {
        List<Trigger> readAsAbilities = List.of(Triggers.dies(ObjectRelation.THIS),
                Triggers.creatureDies(ObjectRelation.YOU_CONTROL), Triggers.sacrificed(ObjectRelation.THIS),
                Triggers.leavesBattlefield(ObjectRelation.THIS), Triggers.milled(ObjectRelation.THIS),
                Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                Triggers.enters(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS),
                Triggers.phasesOut(null, ObjectRelation.THIS), Triggers.transformsToBackFace(ObjectRelation.THIS),
                Triggers.turnedFaceUp(null, ObjectRelation.THIS));
        assertThat(readAsAbilities).allSatisfy(trigger -> {
            TriggeredAbility ability = new TriggeredAbility(trigger, DRAW);
            assertThat(ability.hasOwnEntry()).as(trigger.toString()).isFalse();
            assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        });
        assertThat(Triggers.enters(ObjectRelation.THIS).ownEntry()).isTrue();
        assertThat(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), DRAW).hasOwnEntry()).isTrue();
        assertThat(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS).ownEntry()).isFalse();

        Card card = new Card();
        card.addTrigger(Triggers.dies(ObjectRelation.THIS), DRAW);
        card.addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), GAIN);
        assertThat(card.getTriggerEffects(Triggers.dies(ObjectRelation.THIS))).containsExactly(DRAW);
    }

    @Test
    void anAbilityOfWhenThisEntersAndAnotherZoneChangeIsReadAsItsEffectsForTheOwnEntry() {
        ControllerTurn yourTurn = new ControllerTurn();
        Card card = new Card();
        card.addTrigger(List.of(Triggers.enters(ObjectRelation.THIS),
                        Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.DEFAULT.withInterveningIf(yourTurn), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();

        assertThat(ability.hasOwnEntry()).isTrue();
        assertThat(card.getOwnEntryAbilities()).containsExactly(ability);
        assertThat(card.getOwnEntryEffects()).containsExactly(new ConditionalEffect(yourTurn, DRAW));
    }

    @Test
    void whatTheTriggerPipelineCannotReadIsRejectedAsTheAbilityIsBuilt() {
        TriggerOptions eachCreature = TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE);
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), eachCreature, DRAW))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ONCE_EACH_TURN_FOR_EACH_CREATURE");
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.phasesOut(null, ObjectRelation.THIS), eachCreature,
                DRAW)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whatTheEntersCollectorCannotReadIsRejectedAsTheAbilityIsBuilt() {
        Trigger enters = Triggers.enters(ObjectRelation.THIS);
        for (TriggerLimit limit : List.of(TriggerLimit.ONCE, TriggerLimit.DO_ONLY_ONCE_EACH_TURN,
                TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE)) {
            assertThatThrownBy(() -> new TriggeredAbility(enters, TriggerOptions.DEFAULT.withLimit(limit), DRAW))
                    .as(limit.name()).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(limit.name());
        }
        TriggerOptions oneOrMore = TriggerOptions.DEFAULT.withMultiplicity(TriggerMultiplicity.ONE_OR_MORE);
        assertThatThrownBy(() -> new TriggeredAbility(enters, oneOrMore, DRAW))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("one event");

        TriggeredAbility limited = new TriggeredAbility(enters,
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        assertThat(limited.ownEntryEffects()).extracting(EffectRegistration::effect).containsExactly(DRAW);
        // "One or more" is the other condition's (Oasis of Renewal's lands leaving your graveyard).
        TriggeredAbility withAnother = new TriggeredAbility(List.of(new TriggerCondition(enters),
                new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN))), oneOrMore,
                List.of(new EffectRegistration(DRAW)));
        assertThat(withAnother.hasOwnEntry()).isTrue();
    }

    @Test
    void theReadersOutsideThePipelineTakeAnAbilityWithoutOptionsAndRejectAnyOther() {
        Card plane = new Card();
        plane.addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANY), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT, DRAW, GAIN);
        plane.addTrigger(Triggers.dies(ObjectRelation.THIS), GAIN);
        TriggeredAbility once = new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility conditioned = new TriggeredAbility(Triggers.enters(Triggers.CREATURE,
                ObjectRelation.YOU_CONTROL), TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW);
        TriggeredAbility two = new TriggeredAbility(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), DRAW, GAIN);

        assertThat(ZoneChangeAbilityReader.printed(plane, Triggers.creatureDies(ObjectRelation.ANY))).hasSize(1);
        assertThat(ZoneChangeAbilityReader.strictEffects(ZoneChangeAbilityReader.printed(plane,
                Triggers.creatureDies(ObjectRelation.ANY)))).containsExactly(DRAW, GAIN);
        assertThat(ZoneChangeAbilityReader.strictEffect(new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), DRAW)))
                .isEqualTo(DRAW);
        assertThatThrownBy(() -> ZoneChangeAbilityReader.strictEffects(List.of(once)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ZoneChangeAbilityReader.strictEffect(conditioned))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ZoneChangeAbilityReader.strictEffect(two)).isInstanceOf(IllegalStateException.class);
    }
}
