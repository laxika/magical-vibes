package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardFromSpellbookToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "26")
public class SalacinderAndSootRascals extends Card {

    public SalacinderAndSootRascals() {
        ConjureCardFromSpellbookToHandEffect conjureChoice = new ConjureCardFromSpellbookToHandEffect(
                List.of(
                        new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("CSP", "96"),
                        new ConjureCardFromSpellbookToHandEffect.CardPrintingReference("EMN", "76")
                ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, conjureChoice);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.ELEMENTAL),
                        List.of(conjureChoice)));
    }
}
