package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.CastForForetellCost;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "98")
@CardRegistration(set = "ECC", collectorNumber = "75")
@CardRegistration(set = "KHM", collectorNumber = "296")
public class HauntingVoyage extends Card {

    public HauntingVoyage() {
        CardPredicate creatureOfThatType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardHasTypeChosenThisResolutionPredicate()));
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(new ConditionalReplacementEffect(
                new CastForForetellCost(),
                new ReturnCardsFromControllerGraveyardToBattlefieldEffect(creatureOfThatType, 2),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(creatureOfThatType)
                        .returnAll(true)
                        .chooseAuraAttachment(true)
                        .build()));

        addCastingOption(new ForetellCast("{5}{B}{B}"));
    }
}
