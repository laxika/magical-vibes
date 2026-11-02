package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "201")
public class EvidenceExaminer extends Card {

    public EvidenceExaminer() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new MayEffect(new CollectEvidenceEffect(4), "Collect evidence 4?"));
        addTrigger(Triggers.youCollectEvidence(), CreateTokenEffect.ofClueToken(1));
    }
}
