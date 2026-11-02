package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfMemoryCounterExiledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetAssassinCreatureCardFromGraveyardWithMemoryCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "45")
@CardRegistration(set = "ACR", collectorNumber = "137")
public class AltaRIbnLaAhad extends Card {

    public AltaRIbnLaAhad() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new ExileTargetAssassinCreatureCardFromGraveyardWithMemoryCounterEffect(),
                new CreateTokenCopiesOfMemoryCounterExiledCreaturesEffect()));
    }
}
