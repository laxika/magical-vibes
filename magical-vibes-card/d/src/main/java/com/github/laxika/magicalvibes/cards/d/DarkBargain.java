package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DOM", collectorNumber = "83")
@CardRegistration(set = "IKO", collectorNumber = "82")
public class DarkBargain extends Card {

    public DarkBargain() {
        addSpellEffect(LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(3, 2));
        addSpellEffect(DealDamageEffect.to(Subjects.you(), 2));
    }
}
