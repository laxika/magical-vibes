package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ShinryuTranscendentRival;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentCreatureThenBoostOthersEffect;
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

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseOpponentCreatureThenBoostOthersEffect(-2, -2));
        addTrigger(Triggers.leavesBattlefield(new PermanentIsChosenPermanentPredicate(),
                        ObjectRelation.ANOTHER),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ShinryuTranscendentRival";
    }
}
