package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "GRN", collectorNumber = "193")
@CardRegistration(set = "MKC", collectorNumber = "217")
public class NotionRain extends Card {

    public NotionRain() {
        addSpellEffect(new SurveilEffect(2));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(DealDamageEffect.to(Subjects.you(), 2));
    }
}
