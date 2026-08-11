package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnTargetAndSacrificedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "THS", collectorNumber = "102")
public class RescueFromTheUnderworld extends Card {

    public RescueFromTheUnderworld() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        addEffect(EffectSlot.SPELL, new RegisterDelayedReturnTargetAndSacrificedCardsEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
