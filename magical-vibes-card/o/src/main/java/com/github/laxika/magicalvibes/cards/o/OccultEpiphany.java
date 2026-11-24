package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForDistinctDiscardedCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "VOC", collectorNumber = "52")
public class OccultEpiphany extends Card {

    public OccultEpiphany() {
        // Draw X cards, then discard X cards. Create a 1/1 white Spirit creature token with flying for each card type
        // among cards discarded this way.
        addSpellEffect(new DrawCardEffect(new XValue()));
        addSpellEffect(new DiscardEffect(new XValue(), Subjects.you()));
        addSpellEffect(new CreateTokensForDistinctDiscardedCardTypesEffect(CreateTokenEffect.whiteSpirit(1)));
    }
}
