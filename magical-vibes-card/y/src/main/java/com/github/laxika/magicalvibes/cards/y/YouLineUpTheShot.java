package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "70")
public class YouLineUpTheShot extends Card {

    public YouLineUpTheShot() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Aim for the Wyvern — Conjure a card named Plummet into your hand",
                        new ConjureCardNamedIntoHandEffect("Plummet", false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Aim for the Cursed Amulet — Conjure a card named Naturalize into your hand",
                        new ConjureCardNamedIntoHandEffect("Naturalize", false)),
                new ChooseOneEffect.ChooseOneOption("Fire a Warning Shot — Draw a card", new DrawCardEffect())
        )));
    }
}
