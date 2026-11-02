package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TargetWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(TargetWatch.values(), CounterWatch.values(), DamageWatch.values(),
                        CombatWatch.values(), SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void everyTargetWatchReadsTheBattlefield() {
        assertThat(TargetWatch.values()).allSatisfy(watch -> assertThat(watch.zone()).isEqualTo(Zone.BATTLEFIELD));
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesOfItsTriggerFunctioningFromTheBattlefield() {
        Card card = new Card();
        CounterUnlessPaysEffect ward = new CounterUnlessPaysEffect(2);
        DrawCardEffect fromGraveyard = new DrawCardEffect(1);
        DrawCardEffect onSpell = new DrawCardEffect(2);
        card.addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), ward);
        card.addTrigger(new TriggerCondition(Triggers.becomesTargetOfOpponentSpellOrAbility(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, fromGraveyard);
        card.addTrigger(Triggers.becomesTargetOfOpponentSpell(), onSpell);

        assertThat(TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.effectsOf(card)).containsExactly(ward);
        assertThat(TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY.effectsOf(card)).containsExactly(onSpell);
        assertThat(TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.effectsOf(card)).isEmpty();
    }
}
