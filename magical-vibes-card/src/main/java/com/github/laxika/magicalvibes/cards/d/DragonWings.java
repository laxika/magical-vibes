package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "34")
public class DragonWings extends Card {

    public DragonWings() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE));
        addCycling("{1}{U}");
        addEffect(EffectSlot.GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardMinManaValuePredicate(6),
                        new MayEffect(
                                new ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect(),
                                "Return Dragon Wings from your graveyard to the battlefield attached to that creature?")));
    }
}
