package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "34")
@CardRegistration(set = "AKR", collectorNumber = "42")
public class TrialOfSolidarity extends Card {

    public TrialOfSolidarity() {
        // When this enchantment enters, creatures you control get +2/+1 and gain vigilance until end of turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostAllOwnCreaturesEffect(2, 1),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES));

        // When a Cartouche you control enters, return this enchantment to its owner's hand.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ENCHANTMENT,
                                new PermanentHasSubtypePredicate(CardSubtype.CARTOUCHE))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                ReturnToHandEffect.self());
    }
}
