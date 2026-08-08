package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "68")
public class EmmaraTandris extends Card {

    public EmmaraTandris() {
        // "Prevent all damage that would be dealt to creature tokens you control."
        addEffect(EffectSlot.STATIC, new PreventAllDamageToCreaturesYouControlEffect(
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentIsTokenPredicate()))));
    }
}
