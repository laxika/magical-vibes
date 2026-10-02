package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "C18", collectorNumber = "26")
public class SaheelisDirective extends Card {

    public SaheelisDirective() {
        addEffect(EffectSlot.SPELL, new LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect(
                null, new CardTypePredicate(CardType.ARTIFACT)));
    }
}
