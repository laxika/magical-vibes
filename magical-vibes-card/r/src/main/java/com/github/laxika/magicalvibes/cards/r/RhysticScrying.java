package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "PCY", collectorNumber = "44")
public class RhysticScrying extends Card {

    public RhysticScrying() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(UnlessPaysEffect.mayPay(Subjects.eachPlayer(), "{2}", new DiscardEffect(3, Subjects.you()))
                .withPrompt("Pay {2} to make the caster discard three cards?"));
    }
}
