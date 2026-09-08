package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "6")
public class CemeteryProtector extends Card {

    public CemeteryProtector() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());

        CardSharesCardTypeWithImprintedCardPredicate sharedType =
                new CardSharesCardTypeWithImprintedCardPredicate(true);
        CardEffect createHuman = new ConditionalEffect(
                new ImprintedCardMatches(new CardTruePredicate(), "an exiled card"),
                new CreateTokenEffect("Human", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN), Set.of(), Set.of()));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(sharedType, List.of(createHuman)));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new TriggeringCardConditionalEffect(sharedType, createHuman));
    }
}
