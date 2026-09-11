package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "86")
public class DragonBreath extends Card {

    public DragonBreath() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(1), new Fixed(0))),
                "{R}: Enchanted creature gets +1/+0 until end of turn."
        ));

        addEffect(EffectSlot.GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardMinManaValuePredicate(6),
                        new MayEffect(
                                new ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect(),
                                "Return Dragon Breath from your graveyard to the battlefield attached to that creature?")));
    }
}
