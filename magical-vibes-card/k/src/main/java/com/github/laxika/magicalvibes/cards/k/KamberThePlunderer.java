package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "137")
@CardRegistration(set = "VOC", collectorNumber = "19")
@CardRegistration(set = "VOC", collectorNumber = "57")
public class KamberThePlunderer extends Card {

    private static final String PARTNER_NAME = "Laurine, the Diversion";

    public KamberThePlunderer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put " + PARTNER_NAME + " into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER
        ));

        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), SequenceEffect.of(
                new GainLifeEffect(1),
                CreateTokenEffect.ofBloodToken(1)
        ));
    }
}
