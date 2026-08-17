package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "198")
public class ConfrontTheUnknown extends Card {

    public ConfrontTheUnknown() {
        PermanentCount cluesYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.CLUE), CountScope.CONTROLLER);
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(cluesYouControl, cluesYouControl));
    }
}
