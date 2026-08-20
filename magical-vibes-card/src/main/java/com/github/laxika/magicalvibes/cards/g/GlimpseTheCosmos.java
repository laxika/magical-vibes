package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "60")
public class GlimpseTheCosmos extends Card {

    public GlimpseTheCosmos() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
        addCastingOption(new GraveyardCast(
                new PermanentHasSubtypePredicate(CardSubtype.GIANT), "{U}", List.of(), null, true));
    }
}
