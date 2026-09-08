package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "SCG", collectorNumber = "117")
public class DragonFangs extends Card {

    public DragonFangs() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.TRAMPLE), GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardMinManaValuePredicate(6),
                        new MayEffect(
                                new ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect(),
                                "Return Dragon Fangs from your graveyard to the battlefield attached to that creature?")));
    }
}
