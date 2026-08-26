package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.WrappedGraveyardStaticEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "JUD", collectorNumber = "66")
public class Filth extends Card {

    public Filth() {
        addEffect(EffectSlot.STATIC, new WrappedGraveyardStaticEffect(new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                new GrantKeywordEffect(Keyword.SWAMPWALK, GrantScope.ALL_OWN_CREATURES))));
    }
}
