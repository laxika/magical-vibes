package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "NPH", collectorNumber = "62")
public class GlisteningOil extends Card {

    public GlisteningOil() {
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // Enchanted creature has infect.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INFECT, GrantScope.ENCHANTED_CREATURE));

        // At the beginning of your upkeep, put a -1/-1 counter on enchanted creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutMinusOneMinusOneCounterOnEnchantedCreatureEffect());

        // When Glistening Oil is put into a graveyard from the battlefield,
        // return Glistening Oil to its owner's hand.
        addEffect(EffectSlot.ON_DEATH, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.HAND,
                new CardIsSelfPredicate(),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                false, true, false, null, false
        ));
    }
}
