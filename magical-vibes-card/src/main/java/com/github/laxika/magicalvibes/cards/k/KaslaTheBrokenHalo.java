package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "4")
@CardRegistration(set = "MOC", collectorNumber = "92")
@CardRegistration(set = "MOC", collectorNumber = "137")
public class KaslaTheBrokenHalo extends Card {

    public KaslaTheBrokenHalo() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardKeywordPredicate(Keyword.CONVOKE),
                List.of(new ScryEffect(2), new DrawCardEffect(1))));
    }
}
