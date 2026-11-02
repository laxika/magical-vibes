package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "227")
@CardRegistration(set = "M3C", collectorNumber = "346")
public class Glimmerpost extends Card {

    public Glimmerpost() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainLifeEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.LOCUS), CountScope.ANY_PLAYER)));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
