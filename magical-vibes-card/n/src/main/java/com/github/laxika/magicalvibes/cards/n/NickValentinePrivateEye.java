package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "35")
@CardRegistration(set = "PIP", collectorNumber = "378")
@CardRegistration(set = "PIP", collectorNumber = "563")
@CardRegistration(set = "PIP", collectorNumber = "906")
public class NickValentinePrivateEye extends Card {

    public NickValentinePrivateEye() {
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate())));

        MayEffect investigate = new MayEffect(CreateTokenEffect.ofClueToken(1), "Investigate?");
        addTrigger(Triggers.creatureDies(
                        new PermanentIsArtifactPredicate(),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                investigate);
    }
}
