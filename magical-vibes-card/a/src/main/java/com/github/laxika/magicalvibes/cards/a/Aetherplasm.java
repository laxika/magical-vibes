package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "GPT", collectorNumber = "22")
@CardRegistration(set = "RVR", collectorNumber = "34")
public class Aetherplasm extends Card {

    public Aetherplasm() {
        addEffect(EffectSlot.ON_BLOCK, new MayEffect(
                new com.github.laxika.magicalvibes.model.effect.ConditionalEffect(
                        new com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield(), SequenceEffect.of(
                        ReturnToHandEffect.self(),
                        PutCardToBattlefieldEffect.blocking(new CardTypePredicate(CardType.CREATURE), "creature")), false),
                "Return Aetherplasm to its owner's hand?"));
    }
}
