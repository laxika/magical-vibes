package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "17")
@CardRegistration(set = "BLC", collectorNumber = "52")
public class HazelsBrewmaster extends Card {

    public HazelsBrewmaster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyardWithSource(),
                CreateTokenEffect.ofFoodToken(1));
        addTrigger(Triggers.attacks(),
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyardWithSource(),
                CreateTokenEffect.ofFoodToken(1));
        addStaticEffect(new GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FOOD)));
    }
}
