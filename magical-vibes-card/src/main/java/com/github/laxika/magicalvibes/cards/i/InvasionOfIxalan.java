package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BelligerentRegisaur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "MOM", collectorNumber = "191")
public class InvasionOfIxalan extends Card {

    public InvasionOfIxalan() {
        setBackFaceCard(new BelligerentRegisaur());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        5, new CardIsPermanentPredicate()));
    }

    @Override
    public String getBackFaceClassName() {
        return "BelligerentRegisaur";
    }
}
