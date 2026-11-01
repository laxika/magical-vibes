package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShinryuTranscendentRival;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentCreatureThenBoostOthersEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfWhenChosenPermanentLeavesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "127")
@CardRegistration(set = "FIN", collectorNumber = "384")
@CardRegistration(set = "FIN", collectorNumber = "455")
@CardRegistration(set = "FIN", collectorNumber = "529")
@CardRegistration(set = "FIN", collectorNumber = "589")
@CardRegistration(set = "FIN", collectorNumber = "591")
public class ZenosYaeGalvus extends Card {

    public ZenosYaeGalvus() {
        setBackFaceCard(new ShinryuTranscendentRival());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseOpponentCreatureThenBoostOthersEffect(-2, -2));
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER),
                new TransformSelfWhenChosenPermanentLeavesEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ShinryuTranscendentRival";
    }
}
