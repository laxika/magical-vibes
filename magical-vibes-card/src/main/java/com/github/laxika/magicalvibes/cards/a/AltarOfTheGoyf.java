package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH2", collectorNumber = "220")
public class AltarOfTheGoyf extends Card {

    public AltarOfTheGoyf() {
        CardTypesAmongCardsInGraveyard cardTypes =
                new CardTypesAmongCardsInGraveyard(CountScope.ANY_PLAYER);
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new ConditionalEffect(new AttacksAlone(),
                new BoostTargetCreatureEffect(cardTypes, cardTypes)));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.LHURGOYF)));
    }
}
