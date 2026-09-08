package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "ONE", collectorNumber = "52")
public class GitaxianAnatomist extends Card {

    public GitaxianAnatomist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.SELF),
                        new ProliferateEffect()),
                        "Tap Gitaxian Anatomist and proliferate?"));
    }
}
