package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "VOC", collectorNumber = "5")
@CardRegistration(set = "VOC", collectorNumber = "43")
public class DrogskolReinforcements extends Card {

    public DrogskolReinforcements() {
        // Other Spirits you control have melee.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.MELEE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));

        // Prevent all noncombat damage that would be dealt to Spirits you control.
        addEffect(EffectSlot.STATIC, PreventDamageToCreaturesEffect.youControl(true,
                new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));
    }
}
