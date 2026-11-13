package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CastAbilityTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);

    private static CastAbility castAbility(SpellTrigger trigger, TriggerOptions options, CardEffect... effects) {
        return CastAbility.of(new TriggeredAbility(trigger, options, effects), trigger.filter());
    }

    @Test
    void aFilteredAbilityIsItsFilterEffectsAndInterveningIf() {
        SpellTrigger creatureSpell = Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE))
                .duringOpponentsTurn();
        TriggeredAbility ability = new TriggeredAbility(creatureSpell,
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()), DRAW, GAIN);

        CastAbility cast = CastAbility.of(ability, creatureSpell.filter());

        assertThat(cast.filter()).isEqualTo(creatureSpell.filter());
        assertThat(cast.effects()).containsExactly(DRAW, GAIN);
        assertThat(cast.interveningIf()).isEqualTo(new SourceIsEnchantment());
        assertThat(cast.targetFilter()).isNull();
        assertThat(cast.ability()).isSameAs(ability);
    }

    @Test
    void theAbilitysOwnTargetFilterIsItsTargetFilter() {
        SpellTrigger anySpell = Triggers.youCastSpell().anySpell();
        TriggeredAbility emblem = new TriggeredAbility(anySpell, DRAW).withTargetFilter(TargetFilters.creature());

        CastAbility cast = CastAbility.of(emblem, anySpell.filter());

        assertThat(cast.targetFilter()).isEqualTo(TargetFilters.creature());
        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, DRAW).targetFilter()).isNull();
    }

    @Test
    void anEventConditionIsCarriedApartFromTheInterveningIf() {
        SpellTrigger anySpell = Triggers.youCastSpell().anySpell();

        CastAbility cast = castAbility(anySpell,
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsEnchantment()), DRAW);

        assertThat(cast.eventCondition()).isEqualTo(new SourceIsEnchantment());
        assertThat(cast.interveningIf()).isNull();
    }

    @Test
    void anAuthoredAbilityIsAnAbilityOfItsEffectsAndOptions() {
        CastAbility cast = new CastAbility(SpellFilter.ANY, List.of(DRAW), new SourceIsEnchantment(),
                TargetFilters.creature(), null);

        assertThat(cast.ability().effects()).extracting(EffectRegistration::effect).containsExactly(DRAW);
        assertThat(cast.ability().options().interveningIf()).isEqualTo(new SourceIsEnchantment());
        assertThat(cast.ability().targetFilter()).isEqualTo(TargetFilters.creature());
    }
}
