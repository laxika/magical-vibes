package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "61")
@CardRegistration(set = "SLD", collectorNumber = "843")
@CardRegistration(set = "MOC", collectorNumber = "252")
public class HaakonStromgaldScourge extends Card {

    public HaakonStromgaldScourge() {
        addCastingOption(GraveyardCast.castOnlyFromGraveyard());
        addStaticEffect(new CastSpellsFromGraveyardEffect(
                new CardSubtypePredicate(CardSubtype.KNIGHT)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(2));
    }
}
