package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "321")
public class ProvisionsMerchant extends Card {

    public ProvisionsMerchant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofFoodToken(1));
        addTrigger(Triggers.attacks(), new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                        SequenceEffect.of(
                                new BoostAllCreaturesEffect(1, 1, new PermanentIsAttackingPredicate()),
                                new GrantKeywordEffect(
                                        Keyword.TRAMPLE,
                                        GrantScope.ALL_CREATURES,
                                        new PermanentIsAttackingPredicate())),
                        "a Food"),
                "Sacrifice a Food?"));
    }
}
