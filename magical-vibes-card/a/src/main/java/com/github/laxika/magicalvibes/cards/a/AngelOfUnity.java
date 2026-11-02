package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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

        addTrigger(Triggers.enters(ObjectRelation.THIS), boost);
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(partyCard, List.of(boost)));
    }
}
