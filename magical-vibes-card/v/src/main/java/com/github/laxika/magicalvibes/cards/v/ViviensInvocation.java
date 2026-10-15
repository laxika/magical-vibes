package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.ChosenPermanentDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M19", collectorNumber = "209")
public class ViviensInvocation extends Card {

    public ViviensInvocation() {
        SpellTarget target = target(TargetFilters.creatureAnOpponentControls());
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(7,
                        new CardTypePredicate(CardType.CREATURE)));
        target.addEffect(EffectSlot.SPELL, new ChosenPermanentDealsPowerDamageToTargetCreatureEffect());
    }
}
