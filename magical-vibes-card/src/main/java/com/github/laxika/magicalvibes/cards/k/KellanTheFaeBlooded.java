package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BirthrightBoon;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "WOE", collectorNumber = "230")
public class KellanTheFaeBlooded extends Card {

    public KellanTheFaeBlooded() {
        setBackFaceCard(new BirthrightBoon());
        addCastingOption(new AdventureCast("{1}{W}"));
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new AttachmentsOnSource(true, true),
                new Fixed(0),
                GrantScope.OWN_CREATURES
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BirthrightBoon";
    }
}
