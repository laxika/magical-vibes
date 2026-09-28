package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "5")
@CardRegistration(set = "OTC", collectorNumber = "41")
public class ErisRoarOfTheStorm extends Card {

    public ErisRoarOfTheStorm() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new DistinctManaValuesAmongCardsInGraveyard(CountScope.CONTROLLER, false, instantOrSorcery)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, SpellCastTriggerEffect.nth(
                2,
                null,
                List.of(new CreateTokenEffect("Dragon Elemental", 4, 4, CardColor.RED,
                        List.of(CardSubtype.DRAGON, CardSubtype.ELEMENTAL),
                        Set.of(Keyword.FLYING, Keyword.PROWESS), Set.of()))));
    }
}
