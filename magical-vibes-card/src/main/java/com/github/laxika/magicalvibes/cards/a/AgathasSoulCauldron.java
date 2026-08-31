package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "242")
public class AgathasSoulCauldron extends Card {

    public AgathasSoulCauldron() {
        addEffect(EffectSlot.STATIC, new SpendManaAsAnyColorForActivatedAbilitiesEffect());
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfCreatureCardsExiledWithSourceEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTargetCardFromGraveyardPutCounterOnTargetCreatureEffect()),
                "{T}: Exile target card from a graveyard. When a creature card is exiled this way, put a +1/+1 counter on target creature you control.",
                List.of(
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS),
                        TargetFilters.creatureYouControl()),
                2,
                2));
    }
}
