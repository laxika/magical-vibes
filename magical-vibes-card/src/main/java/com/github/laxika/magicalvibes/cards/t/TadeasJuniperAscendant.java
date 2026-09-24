package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;

@CardRegistration(set = "SLD", collectorNumber = "433")
public class TadeasJuniperAscendant extends Card {

    public TadeasJuniperAscendant() {
        // Tadeas has hexproof unless it's attacking.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceIsAttacking()),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));

        // Whenever a creature you control with reach attacks, untap it and restrict its blockers
        // to creatures whose power is no greater than its power for this combat.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.REACH),
                SequenceEffect.of(
                        new UntapTriggeringPermanentEffect(),
                        new MakeCreatureBlockableOnlyByFilterEffect(
                                new PermanentPowerAtMostSourcePowerPredicate(),
                                "creatures with power less than or equal to this creature's power",
                                EffectDuration.UNTIL_END_OF_COMBAT))));

        // Whenever one or more creatures you control deal combat damage to a player, draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new DrawCardEffect(1), false, true));
    }
}
