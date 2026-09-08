package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TMT", collectorNumber = "87")
@CardRegistration(set = "TMT", collectorNumber = "207")
public class CaseyJonesJuryRigJusticiar extends Card {

    public CaseyJonesJuryRigJusticiar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        4, new CardTypePredicate(CardType.ARTIFACT)));
    }
}
