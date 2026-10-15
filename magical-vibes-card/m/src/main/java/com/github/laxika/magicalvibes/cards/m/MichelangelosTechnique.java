package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TMT", collectorNumber = "122")
@CardRegistration(set = "TMT", collectorNumber = "239")
public class MichelangelosTechnique extends Card {

    public MichelangelosTechnique() {
        addSneak("{3}{G}");
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayPutUpToMatchingOntoBattlefieldRestOnBottomRandomWithinTotalManaValue(
                        8, new CardTypePredicate(CardType.CREATURE), 2, 6));
    }
}
