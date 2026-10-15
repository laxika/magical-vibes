package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RangersMerit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastFromOutsideStartingDeckTriggerEffect;

import java.util.List;

/** Ursine Guide // Ranger's Merit (YSOS 11). */
@CardRegistration(set = "YSOS", collectorNumber = "11")
public class UrsineGuideRangersMerit extends Card {

    public UrsineGuideRangersMerit() {
        setBackFaceCard(new RangersMerit());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastFromOutsideStartingDeckTriggerEffect(List.of(
                        new ConjureCardToBattlefieldEffect("Bear Cub"),
                        new BecomePreparedEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "RangersMerit";
    }
}
