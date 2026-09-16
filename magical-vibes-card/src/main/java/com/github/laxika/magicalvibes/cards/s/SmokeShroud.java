package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "69")
public class SmokeShroud extends Card {

    public SmokeShroud() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.NINJA),
                        new MayEffect(
                                new ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect(),
                                "Return Smoke Shroud from your graveyard to the battlefield attached to that creature?")));
    }
}
