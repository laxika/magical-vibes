package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TLA", collectorNumber = "116")
public class RavenEagle extends Card {

    public RavenEagle() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ExileGraveyardCardCreateTokenIfCreatureEffect.upToOne(CreateTokenEffect.ofClueToken(1)));
        addEffect(EffectSlot.ON_ATTACK,
                ExileGraveyardCardCreateTokenIfCreatureEffect.upToOne(CreateTokenEffect.ofClueToken(1)));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)));
    }
}
