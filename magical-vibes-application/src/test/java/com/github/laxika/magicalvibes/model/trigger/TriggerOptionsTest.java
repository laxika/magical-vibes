package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriggerOptionsTest {

    @Test
    void defaultHasNoInterveningIfNoLimitAndTriggersForEachOccurrence() {
        assertThat(TriggerOptions.DEFAULT.interveningIf()).isNull();
        assertThat(TriggerOptions.DEFAULT.limit()).isEqualTo(TriggerLimit.NONE);
        assertThat(TriggerOptions.DEFAULT.multiplicity()).isEqualTo(TriggerMultiplicity.EACH);
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
}
