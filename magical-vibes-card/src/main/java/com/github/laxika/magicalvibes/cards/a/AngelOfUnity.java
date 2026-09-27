package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "2")
public class AngelOfUnity extends Card {

    public AngelOfUnity() {
        var partyCard = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.CLERIC),
                new CardSubtypePredicate(CardSubtype.ROGUE),
                new CardSubtypePredicate(CardSubtype.WARRIOR),
                new CardSubtypePredicate(CardSubtype.WIZARD)));
        var boost = new ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect(
                new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.CREATURE), partyCard)), 1, 1);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, boost);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(partyCard, List.of(boost)));
    }
}
