package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "29")
public class WanderingChampion extends Card {

    public WanderingChampion() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalEffect(
                new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.RED))),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?")));
    }
}
