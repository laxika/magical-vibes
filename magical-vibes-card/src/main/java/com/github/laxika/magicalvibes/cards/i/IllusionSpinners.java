package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "55")
public class IllusionSpinners extends Card {

    public IllusionSpinners() {
        setFlashCastCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FAERIE)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
    }
}
