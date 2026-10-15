package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MKM", collectorNumber = "194")
@CardRegistration(set = "MKM", collectorNumber = "358")
public class CuriousCadaver extends Card {

    public CuriousCadaver() {
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.CLUE),
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect()
                ));
    }
}
