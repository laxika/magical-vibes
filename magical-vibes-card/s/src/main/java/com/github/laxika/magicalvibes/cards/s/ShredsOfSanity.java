package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetInstantAndSorceryCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "EMN", collectorNumber = "141")
@CardRegistration(set = "SIR", collectorNumber = "176")
public class ShredsOfSanity extends Card {

    public ShredsOfSanity() {
        setAllowSharedTargets(true);
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.INSTANT), GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1);
        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.SORCERY), GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1);
        addEffect(EffectSlot.SPELL, new ReturnTargetInstantAndSorceryCardsFromGraveyardToHandEffect());
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_INSTANT_AND_ONE_SORCERY);
    }
}
