package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "107")
public class VeiledApparition extends Card {

    public VeiledApparition() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                null,
                List.of(new BecomeCreatureEffect(3, 3, CardSubtype.ILLUSION, Set.of(Keyword.FLYING))),
                new SourceIsEnchantment()));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceHasSubtype(CardSubtype.ILLUSION),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.UPKEEP_TRIGGERED,
                        new ForcedCostOrElseEffect(
                                new PayManaCost("{1}{U}"),
                                List.of(new SacrificeSelfEffect()),
                                true),
                        GrantScope.SELF)));
    }
}
