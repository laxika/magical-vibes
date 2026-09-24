package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOC", collectorNumber = "241")
public class CreativeTechnique extends Card {

    public CreativeTechnique() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new ShuffleLibraryEffect(false),
                new RevealUntilCardPredicateMayCastWithoutPayingManaEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))));
        addEffect(EffectSlot.ON_SELF_CAST,
                new MayEffect(new DemonstrateEffect(), "Copy Creative Technique?"));
    }
}
