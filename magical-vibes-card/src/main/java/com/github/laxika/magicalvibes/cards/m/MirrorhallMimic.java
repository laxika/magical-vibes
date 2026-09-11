package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GhastlyMimicry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Map;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "68")
public class MirrorhallMimic extends Card {

    public MirrorhallMimic() {
        setBackFaceCard(new GhastlyMimicry());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", Set.of(CardSubtype.SPIRIT), Map.of()));
        addCastingOption(new DisturbCast("{3}{U}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GhastlyMimicry";
    }
}
