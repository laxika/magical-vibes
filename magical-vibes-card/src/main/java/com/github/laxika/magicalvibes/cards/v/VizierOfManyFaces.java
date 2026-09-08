package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;


@CardRegistration(set = "AKH", collectorNumber = "74")
@CardRegistration(set = "AKR", collectorNumber = "87")
public class VizierOfManyFaces extends Card {

    public VizierOfManyFaces() {
        // You may have this creature enter as a copy of any creature on the battlefield, except if this
        // creature was embalmed, the token has no mana cost, it's white, and it's a Zombie in addition
        // to its other types. (The embalm exception is applied only to an embalm token's final copy.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature",
                CardColor.WHITE, CardSubtype.ZOMBIE, true
        ));

        // Embalm {3}{U}{U}. The token is itself a white / Zombie / no-mana-cost copy of Vizier; its own
        // copy-on-enter ability above then re-clones a chosen creature and re-applies that transformation.
        addEmbalm("{3}{U}{U}", "Shapeshifter Cleric");
    }
}
