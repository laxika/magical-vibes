package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TMP", collectorNumber = "284")
@CardRegistration(set = "TPR", collectorNumber = "221")
@CardRegistration(set = "EMA", collectorNumber = "221")
public class EmmessiTome extends Card {

    public EmmessiTome() {
        addActivatedAbility(new ActivatedAbility(true, "{5}", List.of(new DrawCardEffect(2), new DiscardEffect(1, Subjects.you())), "{5}, {T}: Draw two cards, then discard a card."));
    }
}
