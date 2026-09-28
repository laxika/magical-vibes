package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1875")
public class ChulaneTellerOfTales extends Card {

    public ChulaneTellerOfTales() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(
                        new DrawCardEffect(1),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(
                                        new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?"))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(ReturnToHandEffect.target()),
                "{3}, {T}: Return target creature you control to its owner's hand.",
                TargetFilters.creatureYouControl()
        ));
    }
}
