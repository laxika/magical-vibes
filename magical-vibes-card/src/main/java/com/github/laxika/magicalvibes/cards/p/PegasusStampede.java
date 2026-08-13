package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EXO", collectorNumber = "14")
@CardRegistration(set = "TPR", collectorNumber = "24")
public class PegasusStampede extends Card {

    public PegasusStampede() {
        addEffect(EffectSlot.STATIC, new BuybackEffect(new PermanentIsLandPredicate(), "a land"));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Pegasus", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.PEGASUS), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
    }
}
