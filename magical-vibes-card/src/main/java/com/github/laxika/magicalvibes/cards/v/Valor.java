package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnCreaturesFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSB", collectorNumber = "16")
@CardRegistration(set = "JUD", collectorNumber = "32")
public class Valor extends Card {

    public Valor() {
        addEffect(EffectSlot.STATIC, new GrantKeywordToOwnCreaturesFromGraveyardEffect(
                Keyword.FIRST_STRIKE,
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS))));
    }
}
