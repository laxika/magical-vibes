package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "26")
public class SalacinderAndSootRascals extends Card {

    public SalacinderAndSootRascals() {
        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Conjure a card named Rite of Flame into your hand",
                        new ConjureCardNamedIntoHandEffect("Rite of Flame", false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Conjure a card named Take Inventory into your hand",
                        new ConjureCardNamedIntoHandEffect("Take Inventory", false))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(modes));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.ELEMENTAL),
                        List.of(modes)));
    }
}
