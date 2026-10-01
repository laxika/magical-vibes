package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "29")
@CardRegistration(set = "FDN", collectorNumber = "151")
@CardRegistration(set = "DDO", collectorNumber = "36")
@CardRegistration(set = "IMA", collectorNumber = "40")
@CardRegistration(set = "SLD", collectorNumber = "1667")
@CardRegistration(set = "C15", collectorNumber = "85")
@CardRegistration(set = "LCC", collectorNumber = "142")
@CardRegistration(set = "40K", collectorNumber = "191")
@CardRegistration(set = "BLC", collectorNumber = "161")
public class Aetherize extends Card {

    public Aetherize() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate()))));
    }
}
