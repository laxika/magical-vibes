package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "129")
public class HighwayRobbery extends Card {

    public HighwayRobbery() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Discard a card",
                                new DiscardAndDrawCardEffect(1, 2)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Sacrifice a land",
                                List.of(
                                        new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                                                SacrificeRecipient.CONTROLLER).withRecordedSacrificeCount(),
                                        new DrawCardEffect(new Scaled(new EventValue(), 2))))
                )),
                "Discard a card or sacrifice a land to draw two cards?"
        ));
    }
}
