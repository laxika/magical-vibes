package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "26")
public class SalacinderAndSootRascals extends Card {

    public SalacinderAndSootRascals() {
        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Conjure a card named Rite of Flame into your hand",
                        new ConjureCardNamedIntoHandEffect("CSP", "96", false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Conjure a card named Take Inventory into your hand",
                        new ConjureCardNamedIntoHandEffect("EMN", "76", false))));

        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(modes));
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.ELEMENTAL)), modes);
    }
}
