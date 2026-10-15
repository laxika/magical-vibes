package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;

@CardRegistration(set = "BNG", collectorNumber = "97")
public class FlameWreathedPhoenix extends Card {

    public FlameWreathedPhoenix() {
        addEffect(EffectSlot.STATIC, new TributeEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TributeNotPaidEffect(SequenceEffect.of(
                new SetSelfKeywordIndefinitelyEffect(Keyword.HASTE, true),
                new GrantEffectToSourceEffect(EffectSlot.ON_DEATH,
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect()))));
    }
}
