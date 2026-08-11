package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "56")
public class KulrathMystic extends Card {

    public KulrathMystic() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(4),
                List.of(
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)
                )
        ));
    }
}
