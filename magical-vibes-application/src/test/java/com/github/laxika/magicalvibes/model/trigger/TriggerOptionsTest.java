package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriggerOptionsTest {

    @Test
    void defaultHasNoInterveningIfNoLimitAndTriggersForEachOccurrence() {
        assertThat(TriggerOptions.DEFAULT.interveningIf()).isNull();
        assertThat(TriggerOptions.DEFAULT.limit()).isEqualTo(TriggerLimit.NONE);
        assertThat(TriggerOptions.DEFAULT.multiplicity()).isEqualTo(TriggerMultiplicity.EACH);
        assertThat(TriggerOptions.DEFAULT.eventCondition()).isNull();
    }

    @Test
    void withersChangeOneOptionAndLeaveTheOriginalAlone() {
        SourceCardInGraveyard inGraveyard = new SourceCardInGraveyard();

        TriggerOptions options = TriggerOptions.DEFAULT
                .withInterveningIf(inGraveyard)
                .withLimit(TriggerLimit.ONCE_EACH_TURN)
                .withMultiplicity(TriggerMultiplicity.ONE_OR_MORE);

        assertThat(options).isEqualTo(new TriggerOptions(inGraveyard, TriggerLimit.ONCE_EACH_TURN,
                TriggerMultiplicity.ONE_OR_MORE));
        assertThat(TriggerOptions.DEFAULT)
                .isEqualTo(new TriggerOptions(null, TriggerLimit.NONE, TriggerMultiplicity.EACH));
    }

    @Test
    void limitAndMultiplicityAreRequired() {
        assertThatThrownBy(() -> new TriggerOptions(null, null, TriggerMultiplicity.EACH))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TriggerOptions(null, TriggerLimit.NONE, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TriggerOptions.DEFAULT.withInterveningIf(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void anEventConditionIsKeptByTheOtherWithers() {
        ControllerTurn yourTurn = new ControllerTurn();
        SourceCardInGraveyard inGraveyard = new SourceCardInGraveyard();

        TriggerOptions options = TriggerOptions.DEFAULT.withEventCondition(yourTurn)
                .withInterveningIf(inGraveyard)
                .withLimit(TriggerLimit.ONCE_EACH_TURN)
                .withMultiplicity(TriggerMultiplicity.ONE_OR_MORE);

        assertThat(options).isEqualTo(new TriggerOptions(inGraveyard, TriggerLimit.ONCE_EACH_TURN,
                TriggerMultiplicity.ONE_OR_MORE, yourTurn));
        assertThatThrownBy(() -> TriggerOptions.DEFAULT.withEventCondition(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void theLegacyViewGivesTheEventConditionAsATriggerTimeOnlyConditionalInsideTheOtherWrappers() {
        ControllerTurn yourTurn = new ControllerTurn();
        SourceCardInGraveyard inGraveyard = new SourceCardInGraveyard();
        DrawCardEffect draw = new DrawCardEffect();
        GainLifeEffect gain = new GainLifeEffect(1);
        TriggeredAbility plain = new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withEventCondition(yourTurn), draw, gain);
        TriggeredAbility wrapped = new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), TriggerOptions.DEFAULT
                .withEventCondition(yourTurn).withInterveningIf(inGraveyard).withLimit(TriggerLimit.ONCE_EACH_TURN),
                draw);

        assertThat(plain.effects()).extracting(EffectRegistration::effect).containsExactly(draw, gain);
        assertThat(plain.legacyEffects()).extracting(EffectRegistration::effect).containsExactly(
                ConditionalEffect.atTriggerTime(yourTurn, draw), ConditionalEffect.atTriggerTime(yourTurn, gain));
        assertThat(wrapped.legacyEffects()).extracting(EffectRegistration::effect).containsExactly(
                OncePerTurnTriggerEffect.keyed(new ConditionalEffect(inGraveyard,
                        ConditionalEffect.atTriggerTime(yourTurn, draw), true), wrapped.limitKey()));
    }
}
