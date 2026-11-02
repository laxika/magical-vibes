package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "116")
public class RavenEagle extends Card {

    public RavenEagle() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardCreateTokenIfCreatureEffect.upToOne(CreateTokenEffect.ofClueToken(1)));
        addTrigger(Triggers.attacks(),
                ExileGraveyardCardCreateTokenIfCreatureEffect.upToOne(CreateTokenEffect.ofClueToken(1)));
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)));
    }
}
