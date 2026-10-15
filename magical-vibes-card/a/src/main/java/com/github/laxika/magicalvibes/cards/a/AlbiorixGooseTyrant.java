package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WildGooseChase;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "YOTJ", collectorNumber = "19")
public class AlbiorixGooseTyrant extends Card {

    public AlbiorixGooseTyrant() {
        setBackFaceCard(new WildGooseChase());
        addCastingOption(new AdventureCast("{G}{U}"));

        PerpetuallyBoostCardEffect sacrificeBoost = new PerpetuallyBoostCardEffect(this, 1, 1);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(new PermanentIsTokenPredicate(), sacrificeBoost));
        addEffect(EffectSlot.EXILE_ON_CONTROLLER_TOKEN_SACRIFICED, sacrificeBoost);
    }

    @Override
    public String getBackFaceClassName() {
        return "WildGooseChase";
    }
}
