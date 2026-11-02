package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "51")
@CardRegistration(set = "LCC", collectorNumber = "152")
public class DeeprootWaters extends Card {

    public DeeprootWaters() {
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.MERFOLK),
                        List.of(new CreateTokenEffect("Merfolk", 1, 1,
                                CardColor.BLUE,
                                List.of(CardSubtype.MERFOLK),
                                Set.of(Keyword.HEXPROOF),
                                Set.of()))
                )
        );
    }
}
