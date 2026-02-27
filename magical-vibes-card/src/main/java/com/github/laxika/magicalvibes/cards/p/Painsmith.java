package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAndGrantKeywordOnOwnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOM", collectorNumber = "74")
public class Painsmith extends Card {

    public Painsmith() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new BoostAndGrantKeywordOnOwnSpellCastEffect(
                        new CardTypePredicate(CardType.ARTIFACT), 2, 0, Keyword.DEATHTOUCH),
                "Have target creature get +2/+0 and gain deathtouch until end of turn?"
        ));
    }
}
