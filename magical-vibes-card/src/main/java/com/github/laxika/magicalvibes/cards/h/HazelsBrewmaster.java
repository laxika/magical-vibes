package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "BLC", collectorNumber = "17")
@CardRegistration(set = "BLC", collectorNumber = "52")
public class HazelsBrewmaster extends Card {

    public HazelsBrewmaster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyardWithSource());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofFoodToken(1));
        addEffect(EffectSlot.ON_ATTACK,
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyardWithSource());
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofFoodToken(1));
        addEffect(EffectSlot.STATIC,
                new GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FOOD)));
    }
}
