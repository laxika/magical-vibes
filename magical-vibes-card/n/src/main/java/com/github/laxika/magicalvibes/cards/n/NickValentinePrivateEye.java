package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "PIP", collectorNumber = "35")
@CardRegistration(set = "PIP", collectorNumber = "378")
@CardRegistration(set = "PIP", collectorNumber = "563")
@CardRegistration(set = "PIP", collectorNumber = "906")
public class NickValentinePrivateEye extends Card {

    public NickValentinePrivateEye() {
        addEffect(EffectSlot.STATIC,
                new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentIsArtifactPredicate()));

        MayEffect investigate = new MayEffect(CreateTokenEffect.ofClueToken(1), "Investigate?");
        addEffect(EffectSlot.ON_DEATH, investigate);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(new CardTypePredicate(CardType.ARTIFACT), investigate));
    }
}
