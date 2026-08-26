package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShinryuTranscendentRival;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentCreatureThenBoostOthersEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfWhenChosenPermanentLeavesEffect;

@CardRegistration(set = "FIN", collectorNumber = "127")
public class ZenosYaeGalvus extends Card {

    public ZenosYaeGalvus() {
        setBackFaceCard(new ShinryuTranscendentRival());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseOpponentCreatureThenBoostOthersEffect(-2, -2));
        addEffect(EffectSlot.ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD,
                new TransformSelfWhenChosenPermanentLeavesEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ShinryuTranscendentRival";
    }
}
